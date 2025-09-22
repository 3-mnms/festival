package com.teckit.festival.controller;

import com.teckit.festival.controller.api.RecommendFestivalApiSpecification;
import com.teckit.festival.dto.response.AiActivityResponseDTO;
import com.teckit.festival.dto.response.NearbyFestivalListDTO;
import com.teckit.festival.dto.response.RecommendDTO;
import com.teckit.festival.dto.response.UserGeocodeInfoDTO;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.service.ActivityService;
import com.teckit.festival.service.UserGeocodeService;
import com.teckit.festival.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/festival")
@RequiredArgsConstructor
@Tag(name = "거리 기반 페스티벌 추천 API", description = "시용자 위도 경도 get")
public class RecommendFestivalController implements RecommendFestivalApiSpecification {
    private final UserGeocodeService userGeocodeService;
    private final ActivityService activityService;

    @GetMapping("/user/addressInfo")
    public ResponseEntity<SuccessResponse<UserGeocodeInfoDTO>> getUserGeocodeInfo()
    {
        UserGeocodeInfoDTO userGeocodeInfoDTO = userGeocodeService.geoCodeInfo();
        return ApiResponseUtil.success(userGeocodeInfoDTO);
    }

    @GetMapping("/nearby/festivalList")
    public ResponseEntity<SuccessResponse<NearbyFestivalListDTO>> nearByFestivalList(@AuthenticationPrincipal String principal)
    {
        Long userId = Long.parseLong(principal);
        NearbyFestivalListDTO nearbyFestivalListDTO = userGeocodeService.getNearbyFestival(userId);
        return ApiResponseUtil.success(nearbyFestivalListDTO);
    }

    @GetMapping("/nearby/activities")
    public ResponseEntity<SuccessResponse<List<RecommendDTO>>> nearByActivityList(@AuthenticationPrincipal String principal)
    {
        Long userId = Long.parseLong(principal);
        List<RecommendDTO> responseDTO = activityService.getActivities(userId);
        return ApiResponseUtil.success(responseDTO);
    }

}
