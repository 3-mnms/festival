package com.teckit.festival.controller;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.response.FestivalRegisterResponseDTO;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.service.FestivalManageService;
import com.teckit.festival.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/festival")
@RestController
@RequiredArgsConstructor
@Tag(name = "공연 관리 API", description = "공연 등록/수정/삭제 API")
public class FestivalManageController {

    private final FestivalManageService manageService;

    private Long requireUserId(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("잘못된 사용자 ID 형식입니다: " + authentication.getName());
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Operation(summary = "공연 등록", description = "공연 기본정보, 상세정보, 일정과 파일(포스터, 상세이미지)을 함께 등록합니다.")
    @PreAuthorize("hasRole('HOST')")
    @PostMapping(value = "/manage", consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse<FestivalRegisterResponseDTO>> registerFestival(
            Authentication authentication,
            @RequestPart("requestDTO") FestivalRegisterDTO request, // JSON DTO
            @RequestPart(value = "posterFile", required = false) MultipartFile posterFile,
            @RequestPart(value = "contentFiles", required = false) List<MultipartFile> contentFiles
    ) {
        Long userId = requireUserId(authentication);
        FestivalRegisterResponseDTO responseDto =
                manageService.registerFestivalWithDetails(request, userId, posterFile, contentFiles);
        return ApiResponseUtil.success(responseDto, "🎉 공연 등록 성공");
    }

    @Operation(summary = "공연 수정 (주최자)", description = "fid로 공연 기본/상세/일정 및 파일 교체를 수정합니다.")
    @PreAuthorize("hasRole('HOST')")
    @PutMapping(value = "/manage/{fid}", consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse<FestivalRegisterResponseDTO>> updateFestival(
            Authentication authentication,
            @PathVariable String fid,
            @RequestPart("requestDTO") FestivalRegisterDTO request,
            @RequestPart(value = "posterFile", required = false) MultipartFile posterFile,
            @RequestPart(value = "contentFiles", required = false) List<MultipartFile> contentFiles
    ) {
        Long userId = requireUserId(authentication);
        FestivalRegisterResponseDTO responseDto =
                manageService.updateFestival(fid, request, userId, posterFile, contentFiles);
        return ApiResponseUtil.success(responseDto, "✏️ 공연 수정 성공");
    }

    @Operation(summary = "공연 삭제 (주최자/운영자)")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @DeleteMapping("/manage/{fid}")
    public ResponseEntity<SuccessResponse<Void>> deleteFestivalByHost(
            @PathVariable String fid,
            Authentication authentication
    ) {
        Long userId = requireUserId(authentication);
        boolean admin = isAdmin(authentication);
        manageService.deleteFestivalByHost(fid, userId, admin);
        return ApiResponseUtil.success(null, "🗑️ 공연 삭제 성공");
    }

    @Operation(
            summary = "공연 목록 조회 (주최자/운영자)",
            description = "예시: `/manage?page=0&size=5&keyword=오페라`"
    )
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @GetMapping("/manage")
    public ResponseEntity<SuccessResponse<Page<FestivalRegisterResponseDTO>>> getMyFestivals(
            Authentication authentication,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String keyword
    ) {
        Long userId = requireUserId(authentication);
        boolean admin = isAdmin(authentication);

        Page<FestivalRegisterResponseDTO> response =
                manageService.getFestivalsByRole(userId, admin, keyword, pageable);

        return ApiResponseUtil.success(response, "📄 공연 목록 조회 성공");
    }


    @Operation(summary = "공연 상세 조회")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @GetMapping("/manage/{fid}")
    public ResponseEntity<SuccessResponse<FestivalRegisterResponseDTO>> getFestivalDetail(
            Authentication authentication,
            @PathVariable String fid
    ) {
        Long userId = requireUserId(authentication);
        boolean admin = isAdmin(authentication);
        FestivalRegisterResponseDTO responseDto = manageService.getFestivalDetail(fid, userId, admin);
        return ApiResponseUtil.success(responseDto, "🔎 공연 상세 조회 성공");
    }
}
