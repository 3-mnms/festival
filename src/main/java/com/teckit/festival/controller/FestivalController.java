package com.teckit.festival.controller;

import com.teckit.festival.dto.response.FestivalDetailResponse;
import com.teckit.festival.dto.response.FestivalListResponse;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.service.FestivalService;
import com.teckit.festival.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;               // **

import java.util.List;
import java.util.Map;


@Tag(name = "공연 조회 API", description = "공연 목록 / 상세 / 카테고리 / 검색 / 조회수 API")
@RestController
@AllArgsConstructor
@RequestMapping("/api/festival")
public class FestivalController {

    private final FestivalService festivalService;

    @Operation(summary = "공연 검색", description = "장르, 키워드, 또는 둘 다로 공연을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<?>>> searchFestivals(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String keyword
    ) {
        List<?> festivals = List.of();
        if (genre != null && keyword != null) {
            festivals = festivalService.searchByGenreAndKeyword(genre, keyword);
        } else if (genre != null) {
            festivals = festivalService.searchByGenre(genre);
        } else if (keyword != null) {
            festivals = festivalService.searchByKeyword(keyword);
        }
        return ApiResponseUtil.success(festivals);
    }

    @Operation(summary = "카테고리 목록 조회", description = "등록된 공연의 장르 카테고리를 조회합니다.")
    @GetMapping("/categories")
    public ResponseEntity<SuccessResponse<List<String>>> getCategories() {
        List<String> categories = festivalService.getCategories();
        return ApiResponseUtil.success(categories);
    }

    private static final Map<String, String> SORT_MAP = Map.of(
            "fid",    "festivalDetail.id",
            "fdto",   "fdto",
            "fdfrom", "fdfrom",
            "fname",  "fname",
            "genrenm","genrenm"
    );

    private Sort toSort(String sortParam) {
        // 기본: fid desc → 실제 경로로는 festivalDetail.id desc
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Order.desc("festivalDetail.id"));
        }
        // "key,dir" 형식 지원 (예: fid,asc / fdto,desc)
        String[] parts = sortParam.split(",", 2);
        String key = parts[0].trim().toLowerCase();
        String mapped = SORT_MAP.getOrDefault(key, "festivalDetail.id");
        Sort.Direction dir = (parts.length > 1)
                ? Sort.Direction.fromOptionalString(parts[1].trim().toUpperCase()).orElse(Sort.Direction.DESC)
                : Sort.Direction.DESC;
        return Sort.by(new Sort.Order(dir, mapped));
    }

    @Operation(
            summary = "공연 목록 조회",
            description = """
        공연 목록(포스터/이름/기간)을 조회합니다.
        - 페이지: 0 고정
        - 페이지 크기: 15 고정
        - 정렬: sort=필드명[,asc|desc] (허용: fid, fdto, fdfrom, fname, genrenm)
        - 기본 정렬: fid,desc
        """
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<FestivalListResponse>>> getFestivals(
            @RequestParam(required = false) String sort
    ) {
        Sort sortOption = toSort(sort);                      // ← 여기서 'fid'를 'festivalDetail.id'로 매핑
        Pageable pageable = PageRequest.of(0, 15, sortOption);
        Page<FestivalListResponse> page = festivalService.getFestivals(pageable);
        return ApiResponseUtil.success(page, "페스티벌 목록 조회 성공");
    }

    @Operation(summary = "공연 상세 조회", description = "공연 ID(fid)로 상세 정보를 조회합니다.")
    @GetMapping("/{fid}")
    public ResponseEntity<SuccessResponse<FestivalDetailResponse>> getFestivalDetail(
            @PathVariable("fid") String fid // ** (id → fid로 통일)
    ) {
        FestivalDetailResponse detail = festivalService.getFestivalDetail(fid); // **
        return ApiResponseUtil.success(detail);
    }

    @Operation(summary = "공연 조회수 조회", description = "공연 ID(fid)로 현재 조회수를 가져옵니다.")
    @GetMapping("/views/{fid}")
    public ResponseEntity<SuccessResponse<Integer>> getViews(
            @PathVariable("fid") String fid
    ) {
        int views = festivalService.getViews(fid);
        return ApiResponseUtil.success(views);
    }

    @Operation(summary = "공연 조회수 증가", description = "공연 ID(fid)로 조회수를 1 증가시킵니다.")
    @PostMapping("/views/{fid}")
    public ResponseEntity<SuccessResponse<Integer>> increaseViews(
            @PathVariable("fid") String fid
    ) {
        int updated = festivalService.increaseViews(fid);
        return ApiResponseUtil.success(updated, "조회수 증가");
    }
}
