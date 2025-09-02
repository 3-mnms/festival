package com.teckit.festival.service;

import com.teckit.festival.dto.response.*;
import com.teckit.festival.kafka.FestivalKafkaProducer;
import com.teckit.festival.repository.FestivalScheduleRepository;
import com.teckit.festival.util.DateUtil;
import org.springframework.transaction.annotation.Propagation;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.util.FestivalScheduleGenerator;
import com.teckit.festival.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final FavoriteService favoriteService;

    @Value("${festival-api-key}")
    private String festivalApiKey;

    private static final String API_URL = "http://www.kopis.or.kr/openApi/restful/pblprfr";

    /* ===================== 조회 ===================== */

    @Transactional(readOnly = true)
    public Festival getFestivalByFid(String fid) {
        return festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    public Page<FestivalListResponseDTO> getFestivals(Pageable pageable) {
        return festivalRepository.findList(pageable);
    }

    public FestivalDetailResponseDTO getFestivalDetail(String fid) {
        return getFestivalDetail(fid, null);
    }

    @Transactional(readOnly = true)
    public FestivalDetailResponseDTO getFestivalDetail(String fid, Long userId) {
        FestivalDetail d = festivalDetailRepository.findGraphByFid(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        Festival f = d.getFestival();
        List<String> styurls = (d.getContentFile() == null) ? List.of() : d.getContentFile();
        var schedules = festivalScheduleRepository.findByFid(fid);

        long favoriteCount = favoriteService.readCountFavorites(fid);
        boolean favorited = (userId != null) && favoriteService.readFavorites(fid, userId);

        FestivalDetailResponseDTO dto = FestivalDetailResponseDTO.of(f, d, styurls, schedules);
        dto.setFavoriteCount(favoriteCount);
        dto.setFavorited(favorited);

        return dto;
    }

    /* ===================== 자동 수집 ===================== */

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fetchAndSaveFestivalDetail(String mt20id) {
        if (mt20id == null || mt20id.isBlank()) {
            log.warn("mt20id 비어있음 → return");
            return;
        }

        FestivalDetailDTO dto = fetchFestivalDetail(mt20id);
        if (dto == null) {
            log.warn("상세 없음: {} → return", mt20id);
            return;
        }

        Optional<FestivalDetail> existing = festivalDetailRepository.findByIdWithSchedules(mt20id);

        // updatedate를 사용하여 최신 데이터인지 확인
        if (existing.isPresent() && dto.getUpdatedate() != null) {
            try {
                LocalDateTime dtoUpdatedate = parseUpdatedate(dto.getUpdatedate());
                if (dtoUpdatedate.equals(existing.get().getUpdatedate())) {
                    log.info("이미 최신 데이터(id={})", mt20id);
                    return;
                }
            } catch (Exception e) {
                log.error("updatedate 파싱 실패: {} → 계속 진행", dto.getUpdatedate());
            }
        }

        FestivalDetail detail;
        boolean isNew = existing.isEmpty();

        if (isNew) {
            int price = (dto.getTicketPrice() > 0) ? dto.getTicketPrice() : FestivalScheduleGenerator.generateRandomPrice();
            int nop = (dto.getAvailableNOP() > 0) ? dto.getAvailableNOP() : FestivalScheduleGenerator.generateRandomAvailableNOP();
            detail = dto.toEntity(price, nop);

            LocalDate fdfrom = DateUtil.parseDate(dto.getFdfrom());
            LocalDate fdto = DateUtil.parseDate(dto.getFdto());
            detail.setSchedules(FestivalScheduleGenerator.generateRandomSchedules(detail, fdfrom, fdto));
        } else {
            detail = existing.get();
            detail.setFcltyid(dto.getFcltyid());
            detail.setFname(dto.getFname());
            detail.setFdfrom(DateUtil.parseDate(dto.getFdfrom()));
            detail.setFdto(DateUtil.parseDate(dto.getFdto()));
            detail.setFcltynm(dto.getFcltynm());
            detail.setFcast(dto.getFcast());
            detail.setPrfage(dto.getPrfage());
            detail.setStory(dto.getStory());
            detail.setGenrenm(dto.getGenrenm());
            detail.setFstate(dto.getFstate());
            detail.setPosterFile(dto.getPosterFile());
            detail.setContentFile(dto.getContentFile());
            detail.setEntrpsnmH(dto.getEntrpsnmH());
            detail.setRunningTime(dto.getRunningTime());

            // updatedate 업데이트
            if (dto.getUpdatedate() != null) {
                try {
                    LocalDateTime dtoUpdatedate = parseUpdatedate(dto.getUpdatedate());
                    detail.setUpdatedate(dtoUpdatedate);
                } catch (Exception e) {
                    log.error("updatedate 파싱 실패: {} → 업데이트 생략", dto.getUpdatedate());
                }
            }
        }

        FestivalDetail savedDetail = festivalDetailRepository.saveAndFlush(detail);

        String eventType = isNew ? "FESTIVAL_CREATED" : "FESTIVAL_UPDATED";
        try {
            festivalKafkaProducer.send(savedDetail, eventType);
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
        }

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
        log.info("공연 상세 저장 및 갱신 성공(id={})", mt20id);
    }

    // updatedate 파싱을 위한 private 메서드
    private LocalDateTime parseUpdatedate(String updatedateStr) {
        try {
            return LocalDateTime.parse(updatedateStr.substring(0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid updatedate format");
        }
    }


    @Transactional
    public void fetchAndSaveFestivalDetails(List<String> mt20ids) {
        if (mt20ids == null || mt20ids.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "수집할 공연 ID 목록이 비어 있습니다.");
        }
        for (String id : mt20ids) {
            try {
                fetchAndSaveFestivalDetail(id);
            } catch (BusinessException e) {
                log.error("저장 실패(id={}): {}", id, e.getMessage());
            } catch (Exception e) {
                log.error("저장 실패(id={}): {}", id, e.getMessage(), e);
            }
        }
    }

    private FestivalDetailDTO fetchFestivalDetail(String id) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_URL)
                .pathSegment(id)
                .queryParam("service", festivalApiKey)
                .toUriString();

        FestivalDetailListDTO response = fetchAndParseXml(restClient, uri, FestivalDetailListDTO.class);

        if (response == null || response.getFestivalDetailList() == null || response.getFestivalDetailList().isEmpty()) {
            return null;
        }

        FestivalDetailDTO dto = response.getFestivalDetailList().get(0);
        return dto;
    }

    // 기간으로 ID 수집
    public void fetchAndSaveFestivalListAndDetail(String stdate, String eddate) {
        List<String> ids = fetchIdsByPeriod(stdate, eddate);
        log.info("{}~{} ID 수집 완료: {}건", stdate, eddate, ids.size());
        if (ids.isEmpty()) {
            log.warn("수집 기간 내에 공연 ID가 없습니다.");
            return;
        }
        fetchAndSaveFestivalDetails(ids);
    }

    // 기간으로 ID 목록 조회
    public List<String> fetchIdsByPeriod(String stdate, String eddate) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_URL)
                .queryParam("service", festivalApiKey)
                .queryParam("stdate", stdate)
                .queryParam("eddate", eddate)
                .queryParam("cpage", "1")
                .queryParam("rows", "1")
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
        List<Festival> result = festivalRepository.findByGenrenmAndFnameContaining(genre, keyword);
        if (result.isEmpty()) {
            throw new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND, "검색 결과가 없습니다.");
        }
        return result.stream().map(FestivalListResponseDTO::fromEntity).toList();
    }

    public List<FestivalListResponseDTO> searchByGenre(String genre) {
        List<Festival> result = festivalRepository.findByGenrenm(genre);
        if (result.isEmpty()) {
            throw new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND, "해당 장르의 공연이 없습니다.");
        }
        return result.stream().map(FestivalListResponseDTO::fromEntity).toList();
    }

    public List<FestivalListResponseDTO> searchByKeyword(String keyword) {
        List<Festival> result = festivalRepository.findByFnameContaining(keyword);
        if (result.isEmpty()) {
            throw new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND, "해당 키워드의 공연이 없습니다.");
        }
        return result.stream().map(FestivalListResponseDTO::fromEntity).toList();
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