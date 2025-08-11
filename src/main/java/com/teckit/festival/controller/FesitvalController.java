package com.teckit.festival.controller;

import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.service.FestivalService;
import com.teckit.festival.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공연 조회 API", description = "공연 목록 / 상세 / 카테고리 / 검색 / 조회수 API")
@RestController
@AllArgsConstructor
@RequestMapping("api/festival")
public class FesitvalController {

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

    @Operation(summary = "공연 목록 조회", description = "공연 목록을 페이지네이션으로 조회합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<Festival>>> getFestivals(
            @PageableDefault(size = 15, sort = "fdto", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Festival> page = festivalService.getFestivals(pageable);
        return ApiResponseUtil.success(page, "페스티벌 목록 조회 성공");
    }

    @Operation(summary = "공연 상세 조회", description = "공연 ID(fid)로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<FestivalDetail>> getFestivalDetail(@PathVariable String id) {
        FestivalDetail festivalDetail = festivalService.getFestivalDetail(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        return ApiResponseUtil.success(festivalDetail);
    }

    @Operation(summary = "공연 조회수 조회", description = "공연 ID(fid)로 현재 조회수를 가져옵니다.")
    @GetMapping("/views/{id}")
    public ResponseEntity<SuccessResponse<Integer>> getViews(@PathVariable String id) {
        int views = festivalService.getViews(id);
        return ApiResponseUtil.success(views);
    }

    @Operation(summary = "공연 조회수 증가", description = "공연 ID(fid)로 조회수를 1 증가시킵니다.")
    @PostMapping("/views/{id}")
    public ResponseEntity<SuccessResponse<Integer>> increaseViews(@PathVariable String id) {
        festivalService.increaseViews(id);
        return ApiResponseUtil.success();
    }
}
