package com.teckit.festival.controller.api;

import com.teckit.festival.dto.request.FestivalReviewRequestDTO;
import com.teckit.festival.dto.response.FestivalReviewResponseDTO;
import com.teckit.festival.dto.response.FestivalReviewResultDTO;
import com.teckit.festival.exception.global.ErrorResponse;
import com.teckit.festival.exception.global.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface FestivalReviewApiSpecification {
    @Operation(summary = "페스티벌 별 전체 기대평 조회",
            description = "페스티벌 별 전체 기대평 조회, pagination ex) GET /api/festival/review/{fId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 별 기대평 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "페스티벌 조회 실패", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "festival을 찾을 수 없음",
                    value = """
                                {
                                   "success": false,
                                   "code": "FESTIVAL_NOT_FOUND",
                                   "message": "공연을 찾을 수 없습니다."
                                 }
                            """
            )
            )
            )
    })
    ResponseEntity<SuccessResponse<FestivalReviewResultDTO>> getReviews(@PathVariable("fId") String fId, @RequestParam(defaultValue = "desc") String sort, @RequestParam(defaultValue = "0") int page);

    @Operation(summary = "페스티벌 별 본인 기대평 조회",
            description = "페스티벌 별 본인 기대평 조회 ex) GET /api/festival/review/{fId}/myReview")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 별 본인 기대평 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "404", description = "페스티벌 조회 실패", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "festival을 찾을 수 없음",
                    value = """
                                {
                                   "success": false,
                                   "code": "FESTIVAL_NOT_FOUND",
                                   "message": "공연을 찾을 수 없습니다."
                                 }
                            """
            )
            )
            )
    })
    ResponseEntity<SuccessResponse<FestivalReviewResponseDTO>> getMyReview(@AuthenticationPrincipal String principal, @PathVariable("fId") String fId);

    @Operation(summary = "페스티벌 기대평 생성",
            description = "페스티벌 기대평 생성, FestivalReviewDTO를 포함해야 합니다. ex) POST /api/festival/review/{festivalId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 기대평 생성 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "필수 입력 사항 위반", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "필수 입력 사항(페스티벌 기대평 내용) 위반",
                    value = """
                                {
                                   "success": false,
                                   "code": "VALIDATION_ERROR",
                                   "message": "%s는 필수 입력사항 입니다."
                                 }
                            """
            )
            )
            ),
            @ApiResponse(responseCode = "409", description = "리뷰 중복 오류", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "사용자는 하나의 공연당 하나의 기대평 작성만 가능합니다.",
                    value = """
                                {
                                   "success": false,
                                   "code": "REVIEW_ALREADY_EXISTS",
                                   "message": "공연 한 개당 하나의 기대평만 생성할 수 있습니다."
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
    ResponseEntity<SuccessResponse<FestivalReviewResponseDTO>> createReview(Authentication authentication, @Valid @RequestBody FestivalReviewRequestDTO festivalReviewRequestDTO, @PathVariable("fId") String fId);

    @Operation(summary = "페스티벌 기대평 수정",
            description = "페스티벌 기대평 수정, FestivalReviewDTO를 포함해야 합니다. ex) PATCH /api/festival/review/{fId}/{rId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 기대평 수정 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "필수 입력 사항 위반", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "필수 입력 사항(페스티벌 기대평 내용) 위반",
                    value = """
                                {
                                   "success": false,
                                   "code": "VALIDATION_ERROR",
                                   "message": "%s는 필수 입력사항 입니다."
                                 }
                            """
            )
            )
            ),
            @ApiResponse(responseCode = "403", description = "리뷰 수정 실패", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "리뷰 수정 실패(작성자만 가능(로그인한 userId값과 리뷰를 작성한 userId값 불일치))",
                    value = """
                                {
                                   "success": false,
                                   "code": "REVIEW_NOT_ALLOWED",
                                   "message": "허용되지 않는 행동입니다. 작성자만이 기대평을 수정 또는 삭제할 수 있습니다."
                                 }
                            """
            )
            )
            ),
            @ApiResponse(responseCode = "404", description = "페스티벌 조회 실패, 리뷰 조회 실패", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "festival을 찾을 수 없음 or 수정하려는 리뷰를 찾을 수 없음",
                    value = """
                                {
                                   "success": false,
                                   "code": "FESTIVAL_NOT_FOUND or REVIEW_NOT_FOUND",
                                   "message": "공연을 찾을 수 없습니다. or 기대평을 찾을 수 없습니다."
                                 }
                            """
            )
            )
            )
    })
    ResponseEntity<SuccessResponse<FestivalReviewResponseDTO>> updateReview(Authentication authentication, @Valid @RequestBody FestivalReviewRequestDTO festivalReviewRequestDTO, @PathVariable("fId") String fId, @PathVariable("rId") Long rId);

    @Operation(summary = "페스티벌 기대평 삭제",
            description = "페스티벌 기대평 삭제, ex) PATCH /api/festival/review/{fId}/{rId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 기대평 삭제 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "403", description = "리뷰 삭제 실패", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "리뷰 삭제 실패(작성자만 가능(admin 제외 admin은 모든 리뷰를 삭제할 수 있습니다.))",
                    value = """
                                {
                                   "success": false,
                                   "code": "REVIEW_NOT_ALLOWED",
                                   "message": "허용되지 않는 행동입니다. 작성자만이 기대평을 수정 또는 삭제할 수 있습니다."
                                 }
                            """
            )
            )
            ),
            @ApiResponse(responseCode = "404", description = "페스티벌 조회 실패, 리뷰 조회 실패", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(
                    summary = "festival을 찾을 수 없음 or 삭제하려는 리뷰를 찾을 수 없음",
                    value = """
                                {
                                   "success": false,
                                   "code": "FESTIVAL_NOT_FOUND or REVIEW_NOT_FOUND",
                                   "message": "공연을 찾을 수 없습니다. or 기대평을 찾을 수 없습니다."
                                 }
                            """
            )
            )
            )
    })
    ResponseEntity<SuccessResponse<Void>> deleteReview(Authentication authentication, @PathVariable("fId") String fId, @PathVariable("rId") Long rId);

}
