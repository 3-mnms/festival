package com.teckit.festival.controller;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.service.FestivalManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.stream.Collectors;

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
        String festivalId = manageService.registerFestivalWithDetails(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🎉 공연 등록 성공",
                "data", festivalId
        ));
    }

    @Operation(summary = "공연 수정", description = "공연 ID를 통해 공연 정보를 수정합니다.")
    @PutMapping("/host/{fid}")
    public ResponseEntity<Map<String, Object>> updateFestival(
            @PathVariable String fid,
            @RequestBody FestivalDTO request
    ) {
        Festival updated = manageService.updateFestival(fid, request);
        FestivalDTO response = FestivalDTO.builder()
                .id(updated.getId())
                .mt20id(updated.getId())
                .prfnm(updated.getFname())
                .prfpdfrom(updated.getFdfrom().toString())
                .prfpdto(updated.getFdto().toString())
                .fcltynm(updated.getFcltynm())
                .poster(updated.getPoster())
                .area(updated.getArea())
                .genrenm(updated.getGenrenm())
                .prfstate(updated.getFstate())
                .build();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "✏️ 공연 수정 성공",
                "data", response
        ));
    }

    @Operation(summary = "공연 삭제 (주최자)", description = "주최자가 공연을 삭제합니다.")
    @DeleteMapping("/host/{fid}")
    public ResponseEntity<Map<String, Object>> deleteFestivalByHost(
            @PathVariable String fid,
            @RequestParam Long hostId
    ) {
        manageService.deleteFestivalByHost(fid, hostId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "🗑️ 공연 삭제 성공"
        ));
    }

    @Operation(summary = "내 공연 목록 조회 (주최자)", description = "주최자가 등록한 공연 목록을 조회합니다.")
    @GetMapping("/host")
    public ResponseEntity<Map<String, Object>> getMyFestivals(@RequestParam Long hostId) {
        List<Festival> list = manageService.getFestivalsByHost(hostId);
        List<FestivalDTO> responseList = list.stream()
                .map(f -> FestivalDTO.builder()
                        .id(f.getId())
                        .mt20id(f.getId())
                        .prfnm(f.getFname())
                        .prfpdfrom(f.getFdfrom().toString())
                        .prfpdto(f.getFdto().toString())
                        .fcltynm(f.getFcltynm())
                        .poster(f.getPoster())
                        .area(f.getArea())
                        .genrenm(f.getGenrenm())
                        .prfstate(f.getFstate())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "📄 내 공연 목록 조회 성공",
                "data", responseList
        ));
    }

    @Operation(summary = "전체 공연 목록 조회 (관리자)", description = "운영자가 전체 공연 목록을 조회합니다.")
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAllFestivals() {
        List<Festival> list = manageService.getAllFestivals();
        List<FestivalDTO> responseList = list.stream().map(f -> FestivalDTO.builder()
                .id(f.getId())
                .mt20id(f.getId())
                .prfnm(f.getFname())
                .prfpdfrom(f.getFdfrom().toString())
                .prfpdto(f.getFdto().toString())
                .fcltynm(f.getFcltynm())
                .poster(f.getPoster())
                .area(f.getArea())
                .genrenm(f.getGenrenm())
                .prfstate(f.getFstate())
                .build()).toList();

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