package com.teckit.festival.controller.api;

import com.teckit.festival.dto.response.NearbyFestivalListDTO;
import com.teckit.festival.dto.response.RecommendDTO;
import com.teckit.festival.dto.response.UserGeocodeInfoDTO;
import com.teckit.festival.exception.global.ErrorResponse;
import com.teckit.festival.exception.global.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

public interface RecommendFestivalApiSpecification {
    @Operation(summary = "사용자 위도 경도 정보 조회",
            description = "사용자 위도 경도 정보 조회, ex) GET /api/festival/user/addressInfo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 위도 경도 정보 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 하지 않은 사용자", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "로그인 하지 않은 사용자",
                    value = """
                                {
                                   "success": false,
                                   "code": "UNAUTHENTICATED",
                                   "message": "로그인한 사용자만 가능합니다."
                                 }
                            """
            )
            )
            ),
            @ApiResponse(responseCode = "404", description = "사용자 주소 정보 확인 불가", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "사용자 주소 정보 확인 불가",
                    value = """
                                {
                                   "success": false,
                                   "code": "USER_GEOCODE_FAIL",
                                   "message": "사용자 주소 정보를 확인할 수 없습니다."
                                 }
                            """
            )
            )
            )
    })
    ResponseEntity<SuccessResponse<UserGeocodeInfoDTO>> getUserGeocodeInfo();

    @Operation(summary = "사용자 주소 근처 페스티벌 조회",
            description = "사용자 주소 근처 페스티벌 조회, ex) GET /api/festival/nearby/festivalList")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 주소 근처 페스티벌 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 주소 정보 확인 불가 or 공연 상세 정보 조회 실패", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "사용자 주소 정보 확인 불가 or 공연 상세 정보 조회 실패",
                    value = """
                                {
                                   "success": false,
                                   "code": "USER_GEOCODE_FAIL or FESTIVAL_DETAIL_NOT_FOUND",
                                   "message": "사용자 주소 정보를 확인할 수 없습니다. or 공연 상세 정보를 찾을 수 없습니다."
                                 }
                            """
            )
            )
            )
    })
    ResponseEntity<SuccessResponse<NearbyFestivalListDTO>> nearByFestivalList(@AuthenticationPrincipal String principal);

    @Operation(summary = "페스티벌 공연장 근처 놀거리, 맛집 조회",
            description = "페스티벌 공연장 근처 놀거리, 맛집 조회, ex) GET /api/festival/nearby/activities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 주소 근처 페스티벌 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "근처 페스티벌 추천 정보 없음", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "근처 페스티벌 추천 정보 없음",
                    value = """
                                {
                                   "success": false,
                                   "code": "NEARBY_FESTIVAL_NOT_FOUND",
                                   "message": "근처 페스티벌을 찾을 수 없습니다."
                                 }
                            """
            )
            )
            ),
            @ApiResponse(responseCode = "502", description = "AI 호출 실패", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "AI 호출 실패",
                    value = """
                                {
                                   "success": false,
                                   "code": "AI_RESPONSE_FAILED",
                                   "message": "AI 응답이 실패했습니다."
                                 }
                            """
            )
            )
            )
    })
    ResponseEntity<SuccessResponse<List<RecommendDTO>>> nearByActivityList(@AuthenticationPrincipal String principal);

}
