package com.teckit.festival.controller;

import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.service.FestivalService;
import com.teckit.festival.util.ApiResponseUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/festival")
public class FestivalController {
    private final FestivalService festivalService;

    // 장르와 키워드(가수/제목)로 검색
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<?>>> searchFestivals(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String keyword
    ) {
        List<?> festivals = List.of();  // 빈 리스트 초기화
        if (genre != null && keyword != null) {
            festivals = festivalService.searchByGenreAndKeyword(genre, keyword);
        } else if (genre != null) {
            festivals = festivalService.searchByGenre(genre);
        } else if (keyword != null) {
            festivals = festivalService.searchByKeyword(keyword);
        }

        return ApiResponseUtil.success(festivals);
    }

    // 공연 카테고리(장르) 목록 조회
    @GetMapping("/categories")
    public ResponseEntity<SuccessResponse<List<String>>> getCategories() {
        List<String> categories = festivalService.getCategories();
        return ApiResponseUtil.success(categories);
    }

    // 페스티벌 목록 조회 (페이지네이션)
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<Festival>>> getFestivals(
            @PageableDefault(size = 15, sort = "fdto", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // ⛔️ [오타] 'fdto' → 'fid' 또는 실제 정렬 필드명으로 수정 필요
        Page<Festival> page = festivalService.getFestivals(pageable);
        return ApiResponseUtil.success(page, "페스티벌 목록 조회 성공");
    }

    // 페스티벌 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<FestivalDetail>> getFestivalDetail(@PathVariable String id) {
        FestivalDetail festivalDetail = festivalService.getFestivalDetail(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        return ApiResponseUtil.success(festivalDetail);
    }

    // 조회수 조회
    @GetMapping("/views/{id}")
    public ResponseEntity<SuccessResponse<Integer>> getViews(@PathVariable String id) {
        int views = festivalService.getViews(id);
        return ApiResponseUtil.success(views);
    }

    // 조회수 증가
    @PostMapping("/views/{id}")
    public ResponseEntity<SuccessResponse<Object>> increaseViews(@PathVariable String id) {
        festivalService.increaseViews(id);
        return ApiResponseUtil.success();
    }
}
