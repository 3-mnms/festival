package com.teckit.festival.controller;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.service.FestivalManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "공연 관리 API", description = "공연 등록/수정/삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festival/manage")
public class FestivalManageController {

    private final FestivalManageService manageService;

    @Operation(summary = "공연 등록", description = "공연 기본정보, 상세정보, 일정을 통합 등록합니다.")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<Map<String, Object>> registerFestival(
            java.security.Principal principal,
            @RequestBody FestivalRegisterDTO request
    ) {
        Long userId = Long.parseLong(principal.getName()); // ← 헤더 대신 Principal
        String fid = manageService.registerFestivalWithDetails(request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🎉 공연 등록 성공",
                "data", fid
        ));
    }

    @Operation(summary = "공연 수정", description = "공연 fid(PF000001 등)를 통해 공연 기본/상세/일정 정보를 수정합니다.")
    @PreAuthorize("hasRole('HOST')")
    @PutMapping("/{fid}")
    public ResponseEntity<Map<String, Object>> updateFestival(
            java.security.Principal principal,
            @PathVariable String fid,
            @RequestBody FestivalRegisterDTO request
    ) {
        Long userId = Long.parseLong(principal.getName());
        var updated = manageService.updateFestival(fid, request, userId);
        var response = FestivalDTO.fromEntity(updated);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✏️ 공연 수정 성공",
                "data", response
        ));
    }

    @Operation(summary = "공연 삭제 (주최자)", description = "주최자는 자신이 등록한 공연을 삭제하고, 운영자는 전체 공연 목록을 삭제합니다.")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    @DeleteMapping("/{fid}")
    public ResponseEntity<Map<String, Object>> deleteFestivalByHost(
            @PathVariable String fid,
            java.security.Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());
        manageService.deleteFestivalByHost(fid, userId); // 서비스에서 소유자 일치 검사
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🗑️ 공연 삭제 성공"
        ));
    }

    @Operation(summary = "공연 목록 조회 (주최자)", description = "주최자는 자신이 등록한 공연을 조회하고, 운영자는 전체 공연 목록을 조회합니다.")
    @PreAuthorize("hasAnyRole('HOST','ADMIN')")
    public ResponseEntity<Map<String, Object>> getMyFestivals(java.security.Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        List<FestivalDTO> responseList = manageService.getFestivalsByHost(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "📄 공연 목록 조회 성공",
                "data", responseList
        ));
    }
}
