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
import com.teckit.festival.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return getFestivalDetail(fid, null); // 사용자 정보 없음 → favorited=false 로 내려감
    }

    @Transactional(readOnly = true)
    public FestivalDetailResponseDTO getFestivalDetail(String fid, Long userId) {
        // 기존의 EntityNotFoundException 대신 BusinessException 사용
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

        // 1. 상세 API 호출
        FestivalDetailDTO dto = fetchFestivalDetail(mt20id);
        if (dto == null) {
            log.warn("상세 없음: {} → return", mt20id);
            return;
        }

        // 2. 기존 데이터 로딩 (스케줄 포함)
        Optional<FestivalDetail> existing = festivalDetailRepository.findByIdWithSchedules(mt20id);

        // 3. 업데이트 필요 여부 체크 (updatedate 기준)
        if (existing.isPresent() && dto.getUpdatedate() != null) {
            try {
                // DTO의 String updatedate를 LocalDateTime으로 변환
                LocalDateTime dtoUpdatedate = LocalDateTime.parse(
                        dto.getUpdatedate().substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                // 기존 엔티티의 LocalDateTime updatedate와 비교
                if (dtoUpdatedate.equals(existing.get().getUpdatedate())) {
                    log.info("이미 최신 데이터(id={})", mt20id);
                    return;
                }
            } catch (Exception e) {
                log.error("updatedate 파싱 실패: {} → 계속 진행", dto.getUpdatedate());
                // 파싱 실패 시에는 업데이트가 필요하다고 판단하고 계속 진행
            }
        }

        // 4. Entity 생성 또는 업데이트
        FestivalDetail detail;
        boolean isNew = existing.isEmpty(); // 새로운 데이터인지 여부를 확인

        if (isNew) {
            // 기존 데이터가 없는 경우: 새로운 엔티티를 생성하고 랜덤 값 주입
            int price = (dto.getTicketPrice() > 0) ? dto.getTicketPrice() : FestivalScheduleGenerator.generateRandomPrice();
            int nop = (dto.getAvailableNOP() > 0) ? dto.getAvailableNOP() : FestivalScheduleGenerator.generateRandomAvailableNOP();
            detail = dto.toEntity(price, nop);

            // 스케줄 설정 (최초 생성 시에만)
            LocalDate fdfrom = DateUtil.parseDate(dto.getFdfrom());
            LocalDate fdto = DateUtil.parseDate(dto.getFdto());
            detail.setSchedules(FestivalScheduleGenerator.generateRandomSchedules(detail, fdfrom, fdto));

        } else {
            // 기존 데이터가 있는 경우: 기존 엔티티를 가져와 DTO 값으로 업데이트
            detail = existing.get();
            // toEntity()와 동일한 역할을 하되, 기존에 저장된 price와 nop는 변경하지 않음
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

            // updatedate도 최신 값으로 업데이트
            if (existing.isPresent() && dto.getUpdatedate() != null) {
                try {
                    // 여러 패턴을 처리하기 위해 파서 체인(chain)을 구현합니다.
                    LocalDateTime dtoUpdatedate;

                    try {
                        // 1. 밀리초가 6자리인 경우 시도
                        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                        dtoUpdatedate = LocalDateTime.parse(dto.getUpdatedate(), formatter1);
                    } catch (Exception e1) {
                        try {
                            // 2. 밀리초가 5자리인 경우 시도 (로그에서 확인된 형식)
                            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS");
                            dtoUpdatedate = LocalDateTime.parse(dto.getUpdatedate(), formatter2);
                        } catch (Exception e2) {
                            // 3. 밀리초가 없는 경우 시도
                            DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            dtoUpdatedate = LocalDateTime.parse(dto.getUpdatedate(), formatter3);
                        }
                    }

                    // 기존 데이터와 비교
                    if (dtoUpdatedate.equals(existing.get().getUpdatedate())) {
                        log.info("이미 최신 데이터(id={})", mt20id);
                        return;
                    }
                } catch (Exception e) {
                    // 모든 파싱 시도가 실패한 경우
                    log.error("updatedate 파싱 실패: {} - {} → 계속 진행", dto.getUpdatedate(), e.getMessage());
                    // 파싱 실패 시에는 업데이트가 필요하다고 판단하고 계속 진행
                }
            }
        }

        // 5. 저장
        FestivalDetail savedDetail = festivalDetailRepository.saveAndFlush(detail);

        // 6. Kafka 전송
        String eventType = isNew ? "FESTIVAL_CREATED" : "FESTIVAL_UPDATED";
        try {
            festivalKafkaProducer.send(savedDetail, eventType);
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패: {}", e.getMessage(), e);
        }

        // 7. Festival 테이블 갱신
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
                // 비즈니스 예외는 로그만 남기고 다음 ID로 계속 진행
            } catch (Exception e) {
                log.error("저장 실패(id={}): {}", id, e.getMessage(), e);
                // 일반 예외는 로그를 남기고 다음 ID로 계속 진행
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
    private List<String> fetchIdsByPeriod(String stdate, String eddate) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_URL)
                .queryParam("service", festivalApiKey)
                .queryParam("stdate", stdate)
                .queryParam("eddate", eddate)
                .queryParam("cpage", "1")
                .queryParam("rows", "1") // values * 2 개 조회
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
        // 검색 결과가 없는 경우 NOT_FOUND 예외 발생
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
        // 1. 공연 상세 정보 조회
        FestivalDetail detail = festivalDetailRepository.findByFestivalId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        // 2. 조회수 증가
        detail.setViews(detail.getViews() + 1);

        // 3. 업데이트된 조회수 반환
        return detail.getViews();
    }
}