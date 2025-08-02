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
public class FesitvalController {
    private final FestivalService festivalService;

//    가수, 제목, 장르별
    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<?>>> searchFestivals(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String keyword
    ) {
        List<?> festivals=List.of();
        if (genre != null && keyword != null) {
            festivals = festivalService.searchByGenreAndKeyword(genre, keyword);
        } else if (genre != null) {
            festivals = festivalService.searchByGenre(genre);
        } else if (keyword != null) {
            festivals = festivalService.searchByKeyword(keyword);
        }

        return ApiResponseUtil.success(festivals);
    }


    @GetMapping("/categories")
    public ResponseEntity<SuccessResponse<List<String>>> getCategories(){
        List<String> categories = festivalService.getCategories();
        return ApiResponseUtil.success(categories);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<Festival>>> getFestivals(
            @PageableDefault(size = 15, sort = "fdto", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Festival> page = festivalService.getFestivals(pageable);
        return ApiResponseUtil.success(page, "페스티벌 목록 조회 성공");
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<FestivalDetail>> getFestivalDetail(@PathVariable String id){
        FestivalDetail festivalDetail=festivalService.getFestivalDetail(id)
                .orElseThrow(()->new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        return ApiResponseUtil.success(festivalDetail);
    }

    @GetMapping("/views/{id}")
    public ResponseEntity<SuccessResponse<Integer>> getViews(@PathVariable String id) {
        int views=festivalService.getViews(id);
        return ApiResponseUtil.success(views);
    }

    @PostMapping("/views/{id}")
    public ResponseEntity<SuccessResponse<Integer>> increaseViews(@PathVariable String id) {
        festivalService.increaseViews(id);
        return ApiResponseUtil.success();
    }


}
