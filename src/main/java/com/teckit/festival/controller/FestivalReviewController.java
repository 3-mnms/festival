package com.teckit.festival.controller;

import com.teckit.festival.dto.request.FestivalReviewRequestDTO;
import com.teckit.festival.dto.response.FestivalReviewResponseDTO;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.service.FestivalReviewService;
import com.teckit.festival.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/festival/review")
@RequiredArgsConstructor
@Tag(name = "페스티벌 기대평 API", description = "기대평 조회, 생성, 수정, 삭제")
public class FestivalReviewController {
    private final FestivalReviewService festivalReviewService;

    @GetMapping(value="/{fId}")
    @Operation(summary = "페스티벌 별 전체 기대평 조회",
            description = "페스티벌 별 전체 기대평 조회, pagination ex) GET /api/festival/review/{fId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 별 기대평 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<Page<FestivalReviewResponseDTO>>> getReviews(@PathVariable("fId") String fId, @RequestParam(defaultValue = "desc") String sort, @RequestParam(defaultValue = "0") int page)
    {
        int size = 15;
        int safePage = Math.max(page, 0); //음수로 들어오면 0으로

        Sort.Direction direction = sort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(safePage, size, Sort.by(direction, "createdAt"));

        Page<FestivalReviewResponseDTO> festivalReviewResponseDTOS = festivalReviewService.getReviews(fId, pageable);
        return ApiResponseUtil.success(festivalReviewResponseDTOS);
    }

    @GetMapping(value="/myReview/{fId}")
    @Operation(summary = "페스티벌 별 본인 기대평 조회",
            description = "페스티벌 별 본인 기대평 조회 ex) GET /api/festival/review/myReview/{fId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 별 본인 기대평 조회 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<FestivalReviewResponseDTO>> getMyReview(@AuthenticationPrincipal String principal, @PathVariable("fId") String fId)
    {
        Long userId = Long.parseLong(principal);
        FestivalReviewResponseDTO festivalReviewResponseDTO = festivalReviewService.getMyReview(fId, userId);
        return ApiResponseUtil.success(festivalReviewResponseDTO);
    }

    @PostMapping(value="/{fId}")
    @Operation(summary = "페스티벌 기대평 생성",
            description = "페스티벌 기대평 생성, FestivalReviewDTO를 포함해야 합니다. ex) POST /api/festival/review/{festivalId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 기대평 생성 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<FestivalReviewResponseDTO>> createReview(@AuthenticationPrincipal String principal, @Valid @RequestBody FestivalReviewRequestDTO festivalReviewRequestDTO, @PathVariable("fId") String fId){
        Long userId = Long.parseLong(principal);
        FestivalReviewResponseDTO review = festivalReviewService.createReview(userId, festivalReviewRequestDTO, fId);
        return ApiResponseUtil.success(review);
    }

    @PatchMapping(value="/{fId}/{rId}")
    @Operation(summary = "페스티벌 기대평 수정",
            description = "페스티벌 기대평 수정, FestivalReviewDTO를 포함해야 합니다. ex) PATCH /api/festival/review/{fId}/{rId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 기대평 수정 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<FestivalReviewResponseDTO>> updateReview(@AuthenticationPrincipal String principal, @Valid @RequestBody FestivalReviewRequestDTO festivalReviewRequestDTO, @PathVariable("fId") String fId, @PathVariable("rId") Long rId){
        Long userId = Long.parseLong(principal);
        FestivalReviewResponseDTO review = festivalReviewService.updateReview(userId, festivalReviewRequestDTO, fId, rId);
        return ApiResponseUtil.success(review);
    }

    @DeleteMapping(value="/{fId}/{rId}")
    @Operation(summary = "페스티벌 기대평 삭제",
            description = "페스티벌 기대평 삭제, ex) PATCH /api/festival/review/{fId}/{rId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "페스티벌 기대평 삭제 완료",
                    content = @Content(schema = @Schema(implementation = SuccessResponse.class)))
    })
    public ResponseEntity<SuccessResponse<Void>> deleteReview(Authentication authentication, @PathVariable("fId") String fId, @PathVariable("rId") Long rId){
        Long userId = Long.parseLong(authentication.getName());
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        festivalReviewService.deleteReview(userId, role, fId, rId);
        return ApiResponseUtil.success(null, "기대평 삭제 완료");
    }
}
