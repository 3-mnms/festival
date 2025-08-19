package com.teckit.festival.service;

import com.teckit.festival.dto.response.*;
import com.teckit.festival.kafka.FestivalKafkaProducer;
import com.teckit.festival.repository.FestivalScheduleRepository;
import com.teckit.festival.util.DateUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.util.FestivalScheduleGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teckit.festival.util.XmlApiUtil.fetchAndParseXml;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository festivalDetailRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;

    private final RestClient restClient;
    private final FestivalKafkaProducer festivalKafkaProducer;

    @Value("${festival-api-key}")
    private String festivalApiKey;

    private static final String API_URL = "http://www.kopis.or.kr/openApi/restful/pblprfr";

    /* ===================== 조회 ===================== */

    @Transactional(readOnly = true)
    public Festival getFestivalByFid(String fid) {
        return festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new EntityNotFoundException("공연을 찾을 수 없습니다. fid=" + fid));
    }

    public Page<FestivalListResponseDTO> getFestivals(Pageable pageable) {
        return festivalRepository.findList(pageable); // **(수정) 엔티티 → DTO 페이지**
    }

    public FestivalDetailResponseDTO getFestivalDetail(String fid) {
        FestivalDetail d = festivalDetailRepository.findGraphByFid(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        Festival f = d.getFestival();
        List<String> styurls = (d.getContentFile() == null) ? List.of() : d.getContentFile();

        var schedules = festivalScheduleRepository.findByFid(fid);

        return FestivalDetailResponseDTO.of(f, d, styurls, schedules);
    }

    /* ===================== 자동 수집 ===================== */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fetchAndSaveFestivalDetail(String mt20id) {
        if (mt20id == null || mt20id.isBlank()) {
            log.warn("⚠️ mt20id 비어있음 → return");
            return;
        }

        // 1. 상세 API 호출
        FestivalDetailDTO dto = fetchFestivalDetail(mt20id);
        if (dto == null) {
            log.warn("⚠️ 상세 없음: {} → return", mt20id);
            return;
        }

        // 2. 기존 데이터 schedules까지 fetch join으로 로딩
        Optional<FestivalDetail> existing = festivalDetailRepository.findByIdWithSchedules(mt20id);

        // 3. 업데이트 필요 여부 체크
        if (existing.isPresent()
                && dto.getUpdatedate() != null
                && dto.getUpdatedate().equals(existing.get().getUpdatedate())) {
            return;
        }

        // 4. 가격/좌석수 보정
        int price = (dto.getTicketPrice() > 0) ? dto.getTicketPrice()
                : FestivalScheduleGenerator.generateRandomPrice();
        int nop = (dto.getAvailableNOP() > 0) ? dto.getAvailableNOP()
                : FestivalScheduleGenerator.generateRandomAvailableNOP();

        // 5. Entity 변환
        FestivalDetail detail = dto.toEntity(price, nop);

        // 6. 스케줄 설정 (기존 DB 값이 있으면 그대로 사용)
        if (existing.isPresent()) {
            FestivalDetail existingDetail = existing.get();

            // DB에 이미 스케줄이 있으면 추가 생성 안 함
            if (existingDetail.getSchedules() != null && !existingDetail.getSchedules().isEmpty()) {
                detail.setSchedules(existingDetail.getSchedules()); // 그대로 복사 (필요 시)
            } else {
                // DB에도 없으면 랜덤 생성
                detail.setSchedules(FestivalScheduleGenerator.generateRandomSchedules(detail));
            }
        } else {
            // 기존 Detail이 없으면 무조건 랜덤 생성
            detail.setSchedules(FestivalScheduleGenerator.generateRandomSchedules(detail));
        }

        // 7. 저장
        festivalDetailRepository.saveAndFlush(detail);

        // 8. 재조회하여 PK가 채워진 schedules 가져오기
        FestivalDetail savedDetail = festivalDetailRepository.findByIdWithSchedules(detail.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        // 9. Kafka 전송
        festivalKafkaProducer.send(savedDetail, "FESTIVAL_CREATED");

        // 10. Festival 테이블 갱신
        Festival festival = festivalRepository.findByFestivalDetail_Id(mt20id)
                .orElse(Festival.builder().festivalDetail(savedDetail).build());

        festival.setFname(dto.getFname());
        festival.setFdfrom(DateUtil.parseDate(dto.getFdfrom()));
        festival.setFdto(DateUtil.parseDate(dto.getFdto()));
        festival.setPosterFile(dto.getPosterFile());
        festival.setFcltynm(dto.getFcltynm());
        festival.setGenrenm(dto.getGenrenm());
        festival.setFstate(dto.getFstate());
        festival.setPrfage(dto.getPrfage());

        festivalRepository.save(festival);
    }

    @Transactional
    public void fetchAndSaveFestivalDetails(List<String> mt20ids) {
        for (String id : mt20ids) {
            try {
                fetchAndSaveFestivalDetail(id);
            } catch (Exception e) {
                log.error("❌ 저장 실패(id={}): {}", id, e.getMessage(), e);
            }
        }
    }

    private FestivalDetailDTO fetchFestivalDetail(String id) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_URL)
                .pathSegment(id)
                .queryParam("service", festivalApiKey)
                .toUriString();

        FestivalDetailListDTO response =
                fetchAndParseXml(restClient, uri, FestivalDetailListDTO.class);

        if (response == null
                || response.getFestivalDetailList() == null
                || response.getFestivalDetailList().isEmpty()) {
            return null;
        }
        return response.getFestivalDetailList().get(0);
    }

    // 기간으로 ID 수집
    public void fetchAndSaveFestivalListAndDetail(String stdate, String eddate) {
        List<String> ids = fetchIdsByPeriod(stdate, eddate);
        log.info("📥 {}~{} ID 수집 완료: {}건", stdate, eddate, ids.size());
        fetchAndSaveFestivalDetails(ids);
    }

    // 기간으로 ID 목록 조회
    private List<String> fetchIdsByPeriod(String stdate, String eddate) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_URL)
                .queryParam("service", festivalApiKey)
                .queryParam("stdate", stdate)
                .queryParam("eddate", eddate)
                .queryParam("cpage", "1")
                //100*2 = 200개 수집 (수정 가능)
                .queryParam("rows", "10")
                .toUriString();

        FestivalListDTO list = fetchAndParseXml(restClient, uri, FestivalListDTO.class);
        List<String> ids = new ArrayList<>();
        if (list != null && list.getFestivalList() != null) {
            for (FestivalListItemDTO d : list.getFestivalList()) {
                if (d.getMt20id() != null && !d.getMt20id().isBlank()) {
                    ids.add(d.getMt20id());
                }
            }
        }
        return ids;
    }

    /* ===================== 검색 ===================== */

    public List<FestivalListResponseDTO> searchByGenreAndKeyword(String genre, String keyword) {
        return festivalRepository.findByGenrenmAndFnameContaining(genre, keyword)
                .stream()
                .map(f -> new FestivalListResponseDTO(
                        f.getFestivalDetail().getId(),
                        f.getFname(),
                        f.getFdfrom(),
                        f.getFdto(),
                        f.getPosterFile(),
                        f.getFcltynm(),
                        f.getGenrenm()
                ))
                .toList();
    }

    public List<FestivalListResponseDTO> searchByGenre(String genre) {
        return festivalRepository.findByGenrenm(genre)
                .stream()
                .map(f -> new FestivalListResponseDTO(
                        f.getFestivalDetail().getId(),
                        f.getFname(),
                        f.getFdfrom(),
                        f.getFdto(),
                        f.getPosterFile(),
                        f.getFcltynm(),
                        f.getGenrenm()
                ))
                .toList();
    }

    public List<FestivalListResponseDTO> searchByKeyword(String keyword) {
        return festivalRepository.findByFnameContaining(keyword)
                .stream()
                .map(f -> new FestivalListResponseDTO(
                        f.getFestivalDetail().getId(),
                        f.getFname(),
                        f.getFdfrom(),
                        f.getFdto(),
                        f.getPosterFile(),
                        f.getFcltynm(),
                        f.getGenrenm()
                ))
                .toList();
    }

    /* ===================== 기타 ===================== */

    public List<String> getCategories() {
        return festivalDetailRepository.findDistinctGenrenm();
    }

    public int getViews(String id) {
        FestivalDetail detail = festivalDetailRepository.findByFestivalId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        return detail.getViews();
    }

    @Transactional
    public int increaseViews(String id) {
        FestivalDetail detail = festivalDetailRepository.findByFestivalId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        detail.setViews(detail.getViews() + 1);
        return detail.getViews();
    }
}
