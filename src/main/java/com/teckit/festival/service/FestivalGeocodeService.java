package com.teckit.festival.service;

import com.teckit.festival.dto.response.KakaoResponseDTO;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.enumeration.GeocodeStatus;
import com.teckit.festival.repository.FestivalDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalGeocodeService {
    private final FestivalDetailRepository festivalDetailRepository;
    private final KakaoSearchService kakaoSearchService;

    public String normalize1(String raw) {
        if (raw == null) return null;
        String s = raw;
        s = s.replaceAll("\\[\\s*([^\\]]*?)\\s*\\]", " $1 "); // [서울] -> " 서울 "
        s = s.replaceAll("\\(\\s*([^)]*?)\\s*\\)", " $1 ");   // (오페라극장) -> " 오페라극장 "
        s = s.replaceAll("[.,·/|\\\\]+", " ");
        return s.replaceAll("\\s+", " ").trim();
    }

    //중복 글자 없애기
    public String normalize2(String s) {
        if (s == null) return null;
        String[] tokens = s.split("\\s+");

        LinkedHashSet<String> checkDuplication = new LinkedHashSet<>();
        StringBuilder result = new StringBuilder();
        for (String t : tokens) {
            String key = t.toLowerCase();
            if (!checkDuplication.contains(key)) {
                checkDuplication.add(key);
                if (result.length() > 0)
                    result.append(" ");
                result.append(key);
            }
        }
        return result.toString();
    }

    //45자 자르기
    public String trimTo45Chars(String s) {
        if (s == null) return null;
        if (s.length() > 45) {
            return s.substring(0, 45);
        }
        return s;
    }

    //실패 시 괄호, 괄호 안 텍스트 삭제
    public String normalizeBrackets(String s) {
        if (s == null)
            return null;
        String out = s;
        String check;
        do {
            check = out;
            out = out.replaceAll("\\([^()]*\\)|（[^（）]*）", " ")
                    .replaceAll("\\[[^\\[\\]]*\\]|［[^［］]*］", " ")
                    .replaceAll("\\{[^{}]*\\}|｛[^｛｝]*｝", " ")
                    .replaceAll("<[^<>]*>|＜[^＜＞]*＞", " ");
        } while (!out.equals(check));

        out = out.replaceAll("[\\[\\]\\(\\)\\{\\}<>（）［］｛｝＜＞]", " ");
        out = out.replaceAll("\\s+", " ").trim();
        return out;
    }

    @Transactional
    public boolean geocode(FestivalDetail festivalDetail){
        log.info("geocode 시작", festivalDetail.getId());
        String rawKeyword = festivalDetail.getFcltynm();

        String keyword = trimTo45Chars(normalize2(normalize1(rawKeyword)));

        KakaoResponseDTO response = kakaoSearchService.geocodeKeyword(keyword)
                .orElse(null);

        if(response == null){
            log.info("실패(결과없음) → 재시도 준비 (200ms 대기)");
            try {
                Thread.sleep(200);
            }
            catch (Exception e) {
                log.error("200ms 대기 오류", e);
            }

            String keyword2 = trimTo45Chars(normalizeBrackets(rawKeyword));
            response = kakaoSearchService.geocodeKeyword(keyword2)
                    .orElse(null);
        }

        if(response != null) {
            festivalDetail.setLongitude(Double.parseDouble(response.getLongitude()));
            festivalDetail.setLatitude(Double.parseDouble(response.getLatitude()));
            festivalDetail.setFaddress(response.getAddressName());
            festivalDetail.setIsGeocoded(GeocodeStatus.SUCCESS);
            festivalDetailRepository.save(festivalDetail);
            return true;
        }
        else
            return false;
    }

    @Transactional
    public int geocodeBatch(int size){
        List<FestivalDetail> festivalDetails = festivalDetailRepository.findGeocoding(PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id")));
        log.info("[GEOCODE] picked targets={}", festivalDetails.size());   // ★핵심

        int success = 0;
        for(FestivalDetail festivalDetail: festivalDetails){
            log.info("[GEOCODE] start id={}, name={}", festivalDetail.getId(), festivalDetail.getFcltynm());
            try {
                if (geocode(festivalDetail))
                    success++;
                else {
                    log.info("geocode 실패 (결과 없음)");
                    festivalDetail.setIsGeocoded(GeocodeStatus.NO_RESULT);
                    festivalDetailRepository.save(festivalDetail);
                }
                Thread.sleep(200);
            } catch (Exception e) {
                log.error("geocode 실패(오류 발생)", e);
            }
        }

        log.info("success: {}", success);
        return success;
    }
}
