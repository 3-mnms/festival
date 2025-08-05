package com.teckit.festival.controller;

import com.teckit.festival.entity.Festival;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.mapper.FestivalMapper;
import com.teckit.festival.service.FestivalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "공연 관리 API", description = "주최자 및 관리자의 공연 등록/수정/삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/festivals")
public class FestivalManageController {

    private final FestivalService festivalService;
    private final FestivalMapper festivalMapper;

    // 주최자 - 공연 등록
    @Operation(summary = "공연 등록 (호스트)", description = "주최자가 새로운 공연을 등록합니다.")
    @PostMapping("/host")
    public ResponseEntity<Map<String, Object>> createFestival(@RequestBody FestivalDTO request) {
        Festival created = festivalService.createFestivalWithDetail(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🎉 공연 등록 성공",
                "data", festivalMapper.toDto(created)
        ));
    }

    // 주최자 - 공연 수정
    @Operation(summary = "공연 수정 (호스트)", description = "주최자가 본인이 등록한 공연 정보를 수정합니다.")
    @PutMapping("/host/{fid}")
    public ResponseEntity<Map<String, Object>> updateFestival(
            @PathVariable String fid,
            @RequestBody FestivalDTO request
    ) {
        Festival updated = festivalService.updateFestival(fid, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✏️ 공연 수정 성공",
                "data", festivalMapper.toDto(updated)
        ));
    }

    // 주최자 - 공연 삭제
    @Operation(summary = "공연 삭제 (호스트)", description = "주최자가 본인이 등록한 공연을 삭제합니다.")
    @DeleteMapping("/host/{fid}")
    public ResponseEntity<Map<String, Object>> deleteFestival(
            @PathVariable String fid,
            @RequestParam Long hostId  // 주최자 ID를 쿼리파라미터로 받음
    ) {
        festivalService.deleteFestivalByHost(fid, hostId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🗑️ 공연 삭제 성공"
        ));
    }

    // 주최자 - 내 공연 목록 조회
    @Operation(summary = "내 공연 목록 조회 (호스트)", description = "주최자가 본인이 등록한 공연 목록을 조회합니다.")
    @GetMapping("/host")
    public ResponseEntity<Map<String, Object>> getMyFestivals(@RequestParam Long hostId) {
        List<Festival> list = festivalService.getFestivalsByHost(hostId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "📄 내 공연 목록 조회 성공",
                "data", festivalMapper.toDtoList(list)
        ));
    }

    // 관리자 - 전체 공연 목록 조회
    @Operation(summary = "전체 공연 목록 조회 (관리자)", description = "운영자가 전체 공연 목록을 조회합니다.")
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAllFestivals() {
        List<Festival> list = festivalService.getAllFestivals();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "📋 전체 공연 목록 조회 성공",
                "data", festivalMapper.toDtoList(list)
        ));
    }

    // 관리자 - 공연 삭제
    @Operation(summary = "공연 삭제 (관리자)", description = "운영자가 특정 공연을 삭제합니다.")
    @DeleteMapping("/admin/{fid}")
    public ResponseEntity<Map<String, Object>> adminDeleteFestival(@PathVariable String fid) {
        festivalService.adminDeleteFestival(fid);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🗑️ 공연 삭제 성공 (관리자)"
        ));
    }
}
