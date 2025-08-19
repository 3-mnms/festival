package com.teckit.festival.controller;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.dto.response.FestivalDetailDTO; // FestivalDetailDTO import 추가
import com.teckit.festival.service.FestivalManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @Operation(summary = "공연 등록", description = "공연 기본정보, 상세정보, 일정을 통합 등록합니다.")
    @PreAuthorize("hasRole('HOST')")
    @PostMapping("/manage")
    public ResponseEntity<Map<String, Object>> registerFestival(
            Authentication authentication,
            @RequestBody FestivalRegisterDTO request
    ) {
        Long userId = requireUserId(authentication);
        // registerFestivalWithDetails 메서드는 FestivalDetailDTO를 반환하도록 수정되었습니다.
        // 그 반환값을 그대로 응답 데이터로 사용합니다.
        FestivalDetailDTO responseDto = manageService.registerFestivalWithDetails(request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🎉 공연 등록 성공",
                "data", responseDto // DTO 객체를 바로 응답
        ));
    }

    @Operation(summary = "공연 수정 (주최자)", description = "공연 fid(PF000001 등)를 통해 공연 기본/상세/일정 정보를 수정합니다.")
    @PreAuthorize("hasRole('HOST')")
    @PutMapping("/manage/{fid}")
    public ResponseEntity<Map<String, Object>> updateFestival(
            Authentication authentication,
            @PathVariable String fid,
            @RequestBody FestivalRegisterDTO request
    ) {
        Long userId = requireUserId(authentication);
        // updateFestival 메서드는 FestivalDetailDTO를 반환하도록 수정되었습니다.
        // 그 반환값을 그대로 응답 데이터로 사용합니다.
        FestivalDetailDTO responseDto = manageService.updateFestival(fid, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✏️ 공연 수정 성공",
                "data", responseDto // DTO 객체를 바로 응답
        ));
    }

    @Operation(summary = "공연 삭제 (주최자/운영자)", description = "주최자는 자신이 등록한 공연을 삭제하고, 운영자는 전체 공연 목록을 삭제합니다.")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @DeleteMapping("/manage/{fid}")
    public ResponseEntity<Map<String, Object>> deleteFestivalByHost(
            @PathVariable String fid,
            Authentication authentication
    ) {
        Long userId = requireUserId(authentication);
        boolean admin = isAdmin(authentication);
        manageService.deleteFestivalByHost(fid, userId, admin);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🗑️ 공연 삭제 성공"
        ));
    }

    @Operation(summary = "공연 목록 조회 (주최자/운영자)", description = "주최자는 자신이 등록한 공연만, 운영자는 전체 공연 목록을 조회합니다.")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @GetMapping("/manage")
    public ResponseEntity<Map<String, Object>> getMyFestivals(Authentication authentication) {
        Long userId = requireUserId(authentication);
        boolean admin = isAdmin(authentication);

        // FestivalDetailDTO 리스트를 반환하도록 수정
        List<FestivalDetailDTO> responseList = manageService.getFestivalsByRole(userId, admin);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "📄 공연 목록 조회 성공",
                "data", responseList
        ));
    }
}