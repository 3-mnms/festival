package com.teckit.festival.service;

import com.teckit.festival.dto.request.AiActivityRequestDTO;
import com.teckit.festival.dto.response.*;
import com.teckit.festival.entity.Activity;
import com.teckit.festival.entity.Course;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.NearbyFestival;
import com.teckit.festival.enumeration.ActivityType;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.ActivityRepository;
import com.teckit.festival.repository.CourseRepository;
import com.teckit.festival.repository.NearbyFestivalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {
    private final KakaoSearchService kakaoSearchService;
    private final WebClient webClient;
    private final NearbyFestivalRepository nearbyFestivalRepository;
    private final ActivityRepository activityRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public List<RecommendDTO> getActivities(Long userId){
        List<NearbyFestival> nearbyList = nearbyFestivalRepository.findByUserIdOrderByDistanceAsc(userId);
        if(nearbyList.isEmpty()){
            throw new BusinessException(ErrorCode.NEARBY_FESTIVAL_NOT_FOUND);
        }

        List<RecommendDTO> resultDtoList = new ArrayList<>();
        for(NearbyFestival nearbyFestival: nearbyList) {
            FestivalDetail festivalDetail = nearbyFestival.getFestivalDetail();
            List<Activity> restaurantList = activityRepository.findByFestivalDetailAndActivityType(festivalDetail, ActivityType.Restaurant);
            List<Activity> hotPlaceList = activityRepository.findByFestivalDetailAndActivityType(festivalDetail, ActivityType.HotPlace);
            Course course = courseRepository.findByFestivalDetail(festivalDetail);

            if(restaurantList.isEmpty() || hotPlaceList.isEmpty() || course == null) {
                List<KakaoResponseDTO> restaurants = kakaoSearchService.activitySearch("FD6", nearbyFestival.getLongitude(), nearbyFestival.getLatitude(), 3000, 15)
                        .orElse(Collections.emptyList());

                List<KakaoResponseDTO> hotPlaces = kakaoSearchService.activitySearch("AT4", nearbyFestival.getLongitude(), nearbyFestival.getLatitude(), 3000, 15)
                        .orElse(Collections.emptyList());

                AiActivityResponseDTO responseDTO = callAiActivity(restaurants, hotPlaces); //ai 호출

                List<Activity> activityList = AiActivityResponseDTO.convertToActivity(responseDTO, festivalDetail);
                Course courseAI = AiActivityResponseDTO.convertToCourse(responseDTO, festivalDetail);

                activityRepository.saveAll(activityList);
                courseRepository.save(courseAI);

                List<ActivityDTO> aiRestaurantDTO = KakaoResponseDTO.toActivityListDto(responseDTO.getRestaurants(), ActivityType.Restaurant);
                List<ActivityDTO> aiHotPlaceDTO = KakaoResponseDTO.toActivityListDto(responseDTO.getHotPlaces(), ActivityType.HotPlace);

                RecommendDTO resultDTO = RecommendDTO.toDto(festivalDetail.getId(), aiRestaurantDTO, aiHotPlaceDTO, CourseDTO.toDto(courseAI));
                resultDtoList.add(resultDTO);
            }
            else {
                RecommendDTO resultDTO = RecommendDTO.toDto(festivalDetail.getId(), ActivityDTO.toDtoList(restaurantList), ActivityDTO.toDtoList(hotPlaceList), CourseDTO.toDto(course));
                resultDtoList.add(resultDTO);
            }
        }
        return resultDtoList;
    }

    private AiActivityResponseDTO callAiActivity(List<KakaoResponseDTO> restaurants, List<KakaoResponseDTO> hotPlaces) {

        AiActivityRequestDTO activityRequest = AiActivityRequestDTO.builder()
                .restaurants(restaurants)
                .hotPlaces(hotPlaces)
                .build();

        AiActivityResponseDTO response = webClient.post()
                .uri("/activity/recommend")
                .bodyValue(activityRequest)
                .retrieve()
                .bodyToMono(AiActivityResponseDTO.class)
                .block();

        if (response == null) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_FAILED);
        }

        return response;
    }

}
