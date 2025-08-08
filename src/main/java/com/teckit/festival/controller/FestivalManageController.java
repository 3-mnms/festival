package com.teckit.festival.controller;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.service.FestivalManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "공연 관리 API", description = "공연 등록/수정/삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festivals/manage")
public class FestivalManageController {

    private final FestivalManageService manageService;

    @Operation(summary = "공연 등록", description = "공연 기본정보, 상세정보, 일정을 통합 등록합니다.")
    @PostMapping("/host")
    public ResponseEntity<Map<String, Object>> registerFestival(@RequestBody FestivalRegisterDTO request) {
        String fid = manageService.registerFestivalWithDetails(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🎉 공연 등록 성공",
                "data", fid
        ));
    }

    @Operation(summary = "공연 수정", description = "공연 fid(PF000001 등)를 통해 공연 정보를 수정합니다.")
    @PutMapping("/host/{fid}")
    public ResponseEntity<Map<String, Object>> updateFestival(
            @PathVariable String fid,
            @RequestBody FestivalDTO request
    ) {
        // Service가 엔티티를 업데이트하고, 컨트롤러에서는 DTO로 변환해서 반환
        var updated = manageService.updateFestival(fid, request);
        var response = FestivalDTO.fromEntity(updated);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✏️ 공연 수정 성공",
                "data", response
        ));
    }

    @Operation(summary = "공연 삭제 (주최자)", description = "주최자가 자신의 공연을 삭제합니다.")
    @DeleteMapping("/host/{fid}")
    public ResponseEntity<Map<String, Object>> deleteFestivalByHost(
            @PathVariable String fid,
            @RequestParam String loginId
    ) {
        manageService.deleteFestivalByHost(fid, loginId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🗑️ 공연 삭제 성공"
        ));
    }

    @Operation(summary = "내 공연 목록 조회 (주최자)", description = "주최자가 등록한 공연 목록을 조회합니다.")
    @GetMapping("/host")
    public ResponseEntity<Map<String, Object>> getMyFestivals(@RequestParam String loginId) {
        List<FestivalDTO> responseList = manageService.getFestivalsByHost(loginId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "📄 내 공연 목록 조회 성공",
                "data", responseList
        ));
    }

    @Operation(summary = "전체 공연 목록 조회 (관리자)", description = "운영자가 전체 공연 목록을 조회합니다.")
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAllFestivals() {
        List<FestivalDTO> responseList = manageService.getAllFestivals();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "📋 전체 공연 목록 조회 성공",
                "data", responseList
        ));
    }

    @Operation(summary = "공연 삭제 (관리자)", description = "운영자가 특정 공연을 삭제합니다.")
    @DeleteMapping("/admin/{fid}")
    public ResponseEntity<Map<String, Object>> adminDeleteFestival(@PathVariable String fid) {
        manageService.adminDeleteFestival(fid);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🗑️ 공연 삭제 성공 (관리자)"
        ));
    }
}
