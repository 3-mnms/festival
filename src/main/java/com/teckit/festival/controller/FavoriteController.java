package com.teckit.festival.controller;

import com.teckit.festival.dto.response.FavoriteToggleResponse;
import com.teckit.festival.dto.response.MyFavoritesListResponse;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.util.ApiResponseUtil;
import com.teckit.festival.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// 새롭게 추가할 DTO 클래스
// import com.teckit.festival.dto.response.LikedResponse;
// import com.teckit.festival.dto.response.CountResponse;

@Tag(name = "관심 상품 관리 API", description = "관심 상품 등록 / 삭제 / 조회 등의 API")
@RestController
@RequestMapping("/api/festival")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "관심 상품 등록", description = "사용자는 특정 상품에 대해 관심 상품 등록을 합니다.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/favorites/{fid}")
    public ResponseEntity<SuccessResponse<FavoriteToggleResponse>> createFavorites(@PathVariable String fid, Authentication auth) {
        Long userId = toUserId(auth.getPrincipal());
        FavoriteToggleResponse response = favoriteService.createFavorites(fid, userId);
        return ApiResponseUtil.success(response);
    }

    @Operation(summary = "관심 상품 등록 해제", description = "사용자는 특정 상품에 대해 관심 상품 등록 해제를 합니다.")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/favorites/{fid}")
    public ResponseEntity<SuccessResponse<FavoriteToggleResponse>> deleteFavorites(@PathVariable String fid, Authentication auth) {
        Long userId = toUserId(auth.getPrincipal());
        FavoriteToggleResponse response = favoriteService.deleteFavorites(fid, userId);
        return ApiResponseUtil.success(response);
    }

    @Operation(summary = "특정 상품 관심 상품 등록 여부 조회", description = "특정 상품에 대해 사용자들이 관심 상품 등록 여부를 확인 합니다. ")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/favorites/me/{fid}")
    public ResponseEntity<SuccessResponse<Map<String, Boolean>>> readFavorites(@PathVariable String fid, Authentication auth) {
        Long userId = toUserId(auth.getPrincipal());
        boolean isLiked = favoriteService.readFavorites(fid, userId);
        return ApiResponseUtil.success(Map.of("liked", isLiked));
    }

    @Operation(summary = "특정 상품 관심 상품 등록 개수 조회", description = "특정 상품에 대해 사용자들이 관심 상품 등록 수를 확인 합니다. ")
    @PreAuthorize("permitAll()")
    @GetMapping("/favorites/{fid}/count")
    public ResponseEntity<SuccessResponse<Map<String, Long>>> readCountFavorites(@PathVariable String fid) {
        long count = favoriteService.readCountFavorites(fid);
        return ApiResponseUtil.success(Map.of("count", count));
    }

    @Operation(summary = "사용자 관심 상품 조회", description = "사용자는 자신이 등록한 관심 상품을 조회 가능 합니다.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/favorites/me")
    public ResponseEntity<SuccessResponse<MyFavoritesListResponse>> readMyFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        Long userId = toUserId(auth.getPrincipal());
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        MyFavoritesListResponse response = favoriteService.readMyFavorites(userId, pageable);
        return ApiResponseUtil.success(response);
    }

    // 팀 컨벤션: principal이 String or Long 어느 쪽이든 안전 추출
    private Long toUserId(Object principal) {
        if (principal instanceof Number n) return n.longValue();
        return Long.valueOf(String.valueOf(principal));
    }
}