package com.teckit.festival.service;

import com.teckit.festival.dto.response.FestivalDetailDTO;
import com.teckit.festival.dto.response.FestivalDetailListDTO;
import com.teckit.festival.dto.response.FestivalListDTO;
import com.teckit.festival.dto.response.FestivalListItemDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.util.FestivalScheduleGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
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
    private final RestClient restClient;

    @Value("${festival-api-key}")
    private String festivalApiKey;

    /*@PostConstruct
    public void checkApiKey() {
        log.info("🎯 FESTIVAL_API_KEY = {}", festivalApiKey);
    }*/

    private static final String API_URL = "http://www.kopis.or.kr/openApi/restful/pblprfr";

    /* ===================== 조회 ===================== */

    public Page<Festival> getFestivals(Pageable pageable) {
        return festivalRepository.findAll(pageable);
    }

    public Optional<FestivalDetail> getFestivalDetail(String fid) {
        return festivalDetailRepository.findByFestivalId(fid);
    }

    /* ===================== 자동 수집 ===================== */

    @Transactional
    public void fetchAndSaveFestivalDetail(String mt20id) {
        if (mt20id == null || mt20id.isBlank()) {
            log.warn("⚠️ mt20id 비어있음");
            return;
        }

        // 상세 API 호출
        FestivalDetailDTO dto = fetchFestivalDetail(mt20id);
        if (dto == null) {
            log.warn("⚠️ 상세 없음: {}", mt20id);
            return;
        }

        // 업데이트 여부 체크
        Optional<FestivalDetail> existing = festivalDetailRepository.findById(mt20id);
        if (existing.isPresent() &&
                dto.getUpdatedate() != null &&
                dto.getUpdatedate().equals(existing.get().getUpdatedate())) {
            log.info("⏭️ 변경 없음: {}", mt20id);
            return;
        }

        int price = FestivalScheduleGenerator.generateRandomPrice();
        int nop = FestivalScheduleGenerator.generateRandomAvailableNOP();

        FestivalDetail detail = dto.toEntity(price, nop);
        List<FestivalSchedule> schedules = FestivalScheduleGenerator.generateRandomSchedules(detail);
        detail.setSchedules(schedules);

        festivalDetailRepository.save(detail);
        log.info("✅ FestivalDetail 저장: {}", detail.getId());

        // Festival(읽기 전용)
        Festival festival = festivalRepository.findByFestivalDetail_Id(mt20id)
                .orElse(Festival.builder().festivalDetail(detail).build());

        festival.setFname(dto.getPrfnm());
        festival.setFdfrom(dto.getPrfpdfrom());
        festival.setFdto(dto.getPrfpdto());
        festival.setPosterFile(dto.getPoster());
        festival.setFcltynm(dto.getFcltynm());
        festival.setGenrenm(dto.getGenrenm());
        festival.setFstate(dto.getPrfstate());
        festival.setFage(dto.getPrfage());
        festival.setLoginId(dto.getLoginId() != null ? dto.getLoginId() : "SYSTEM");
        festival.setTicketPick(dto.getTicketPick());
        festival.setMaxPurchase(dto.getMaxPurchase());
        festival.setTicketPrice(price);
        festival.setAvailableNOP(nop);

        festivalRepository.save(festival);
        log.info("✅ Festival 저장: {} ({})", festival.getFname(), mt20id);
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

        FestivalDetailListDTO response = fetchAndParseXml(restClient, uri, FestivalDetailListDTO.class);
        if (response == null || response.getFestivalDetailList() == null || response.getFestivalDetailList().isEmpty()) {
            return null;
        }
        return response.getFestivalDetailList().get(0);
    }

    @Transactional
    public void fetchAndSaveFestivalListAndDetail(String stdate, String eddate) {
        List<String> ids = fetchIdsByPeriod(stdate, eddate);
        log.info("📥 {}~{} ID 수집 완료: {}건", stdate, eddate, ids.size());
        fetchAndSaveFestivalDetails(ids);
    }

    private List<String> fetchIdsByPeriod(String stdate, String eddate) {
        String uri = UriComponentsBuilder
                .fromHttpUrl(API_URL)
                .queryParam("service", festivalApiKey)
                .queryParam("stdate", stdate)
                .queryParam("eddate", eddate)
                .queryParam("cpage", "1")
                .queryParam("rows", "100")
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

    public List<Festival> searchByGenreAndKeyword(String genre, String keyword) {
        return festivalRepository.findByGenrenmAndFnameContaining(genre, keyword);
    }

    public List<Festival> searchByGenre(String genre) {
        return festivalRepository.findByGenrenm(genre);
    }

    public List<Festival> searchByKeyword(String keyword) {
        return festivalRepository.findByFnameContaining(keyword);
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
