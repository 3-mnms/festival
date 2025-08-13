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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;               // **

import java.util.List;

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

    @Operation(
            summary = "공연 목록 조회",
            description = "공연 목록(포스터/이름/기간)을 페이지네이션으로 조회합니다. 기본: page=0, size=15, sort=fdto,desc",
            parameters = {                                           // ** Swagger 파라미터 설명
                    @Parameter(name = "page", description = "0부터 시작하는 페이지 인덱스", example = "0"),
                    @Parameter(name = "size", description = "페이지 크기(1~50 권장)", example = "15"),
                    @Parameter(
                            name = "sort",
                            description = "정렬 필드와 방향. 여러 개 가능 (예: fdto,desc 또는 fname,asc)",
                            array = @ArraySchema(schema = @Schema(type = "string")),
                            example = "fdto,desc"
                    )
            }
    )
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<FestivalListResponse>>> getFestivals(
            @ParameterObject                                            // ** Pageable을 page/size/sort로 펼쳐서 노출
            @PageableDefault(size = 15, sort = "fdto", direction = Sort.Direction.DESC) // ** 백엔드 기본값
            Pageable pageable
    ) {
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
