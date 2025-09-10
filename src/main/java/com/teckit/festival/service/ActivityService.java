package com.teckit.festival.service;

import com.teckit.festival.dto.request.AiReviewRequestDTO;
import com.teckit.festival.dto.response.AiReviewResponseDTO;
import com.teckit.festival.dto.response.KakaoResponseDTO;
import com.teckit.festival.entity.NearbyFestival;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {
    private final KakaoSearchService kakaoSearchService;
    private final WebClient webClient;

    @Transactional
    public void getActivities(NearbyFestival nearbyFestival){
        List<KakaoResponseDTO> restaurants = kakaoSearchService.activitySearch("FD6", nearbyFestival.getLongitude(), nearbyFestival.getLatitude(), 3, 15)
                .orElse(null);

        List<KakaoResponseDTO> hotPlaces = kakaoSearchService.activitySearch("AT4", nearbyFestival.getLongitude(), nearbyFestival.getLatitude(), 3, 15)
                .orElse(null);


        AiReviewResponseDTO responseDTO = callAiActivity(restaurants, hotPlaces);

    }

    private AiReviewResponseDTO callAiActivity(List<KakaoResponseDTO> restaurants, List<KakaoResponseDTO> hotPlaces) {

//        AiReviewRequestDTO activityRequest = AiReviewRequestDTO.from();

        AiReviewResponseDTO response = webClient.post()
                .uri("/festival/activity/recommend")
//                .bodyValue(activityRequest)
                .retrieve()
                .bodyToMono(AiReviewResponseDTO.class)
                .block();

        if (response == null) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_FAILED);
        }

        return response;
    }

}
