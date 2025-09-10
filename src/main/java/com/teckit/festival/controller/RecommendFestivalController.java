package com.teckit.festival.controller;

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
public class RecommendFestivalController {
    private final UserGeocodeService userGeocodeService;
    private final ActivityService activityService;

    @GetMapping("/user/addressInfo")
    @Operation(summary = "사용자 위도 경도 정보 조회",
            description = "사용자 위도 경도 정보 조회, ex) GET /api/festival/user/addressInfo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 위도 경도 정보 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<UserGeocodeInfoDTO>> getUserGeocodeInfo()
    {
        UserGeocodeInfoDTO userGeocodeInfoDTO = userGeocodeService.geoCodeInfo();
        return ApiResponseUtil.success(userGeocodeInfoDTO);
    }

    @GetMapping("/nearby/festivalList")
    @Operation(summary = "사용자 주소 근처 페스티벌 조회",
            description = "사용자 주소 근처 페스티벌 조회, ex) GET /api/festival/nearby/festivalList")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 주소 근처 페스티벌 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<NearbyFestivalListDTO>> nearByFestivalList(@AuthenticationPrincipal String principal)
    {
        Long userId = Long.parseLong(principal);
        NearbyFestivalListDTO nearbyFestivalListDTO = userGeocodeService.getNearbyFestival(userId);
        return ApiResponseUtil.success(nearbyFestivalListDTO);
    }

    @GetMapping("/nearby/activities")
    @Operation(summary = "페스티벌 공연장 근처 놀거리, 맛집 조회",
            description = "페스티벌 공연장 근처 놀거리, 맛집 조회, ex) GET /api/festival/nearby/activities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 주소 근처 페스티벌 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<List<RecommendDTO>>> nearByActivityList(@AuthenticationPrincipal String principal)
    {
        Long userId = Long.parseLong(principal);
        List<RecommendDTO> responseDTO = activityService.getActivities(userId);
        return ApiResponseUtil.success(responseDTO);
    }

}
