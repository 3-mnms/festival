package com.teckit.festival.service;

import com.teckit.festival.dto.response.KakaoResponseDTO;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.repository.FestivalDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalGeocodeService {
    private final FestivalDetailRepository festivalDetailRepository;
    private final KakaoSearchService kakaoSearchService;

    @Transactional
    public boolean geocode(FestivalDetail festivalDetail){
        log.debug("geocode 시작", festivalDetail.getId());
        KakaoResponseDTO response = kakaoSearchService.geocodeKeyword(festivalDetail.getFcltynm())
                .orElse(null);

        if(response != null) {
            festivalDetail.setLongitude(Double.parseDouble(response.getLongitude()));
            festivalDetail.setLatitude(Double.parseDouble(response.getLatitude()));
            festivalDetail.setFaddress(response.getAddressName());
            festivalDetail.setGeocoded(true);
            festivalDetailRepository.save(festivalDetail);
            return true;
        }
        else
            return false;
    }

    public int geocodeBatch(int size){
        List<FestivalDetail> festivalDetails = festivalDetailRepository.findGeocoding(PageRequest.of(0, size, Sort.by(Sort.Direction.ASC, "id")));
        int success = 0;
        for(FestivalDetail festivalDetail: festivalDetails){
            try {
                if (geocode(festivalDetail))
                    success++;
                else
                    log.debug("geocode 실패 (결과 없음)");
            }catch (Exception e){
                log.debug("geocode 실패 (예외)");
            }
        }
        return success;
    }
}
