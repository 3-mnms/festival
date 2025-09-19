package com.teckit.festival.controller;

import com.teckit.festival.dto.response.FestivalDetailResponseDTO;
import com.teckit.festival.dto.response.FestivalListResponseDTO;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.service.FestivalGeocodeService;
import com.teckit.festival.service.FestivalService;
import com.teckit.festival.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;


@Tag(name = "공연 조회 API", description = "공연 목록 / 상세 / 카테고리 / 검색 / 조회수 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festival")
public class FestivalController {

    private final FestivalService festivalService;
    private final FestivalGeocodeService festivalGeocodeService;

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

    @Operation(summary = "카테고리 목록/공연 조회", description = "genrenm이 없으면 카테고리 목록, 있으면 해당 공연을 조회합니다.")
    @GetMapping("/categories")
    public ResponseEntity<?> getCategoriesOrFestivals(
            @RequestParam(required = false) String genrenm,
            @PageableDefault(size = 15, sort = "festivalDetail.views", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        if (genrenm == null) {
            List<String> categories = festivalService.getCategories();
            return ApiResponseUtil.success(categories, "카테고리 목록 조회 성공");
        }
        Page<FestivalListResponseDTO> page = festivalService.getFestivalsByCategory(genrenm, pageable);
        return ApiResponseUtil.success(page, "카테고리별 공연 목록 조회 성공");
    }

    // views 정렬 옵션만 허용
    private static final Map<String, String> SORT_MAP = Map.of(
            "views", "festivalDetail.views"
    );

    private Sort toSort(String sortParam) {
        // 기본 정렬을 "views,desc"로 고정합니다.
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Order.desc("festivalDetail.views"));
        }
        // "key,dir" 형식 지원 (예: views,asc / views,desc)
        String[] parts = sortParam.split(",", 2);
        String key = parts[0].trim().toLowerCase();
        String mapped = SORT_MAP.getOrDefault(key, "festivalDetail.views");
        Sort.Direction dir = (parts.length > 1)
                ? Sort.Direction.fromOptionalString(parts[1].trim().toUpperCase()).orElse(Sort.Direction.DESC)
                : Sort.Direction.DESC;
        return Sort.by(new Sort.Order(dir, mapped));
    }

    @Operation(summary = "공연 목록 조회", description = "'공연완료' 제외 공연을 조회수 순으로 정렬합니다.")
    @GetMapping
    public ResponseEntity<SuccessResponse<Page<FestivalListResponseDTO>>> getFestivals(
            @PageableDefault(
                    size = 15,
                    sort = "festivalDetail.views",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @RequestParam(required = false, defaultValue = "false") boolean all
    ) {
        Page<FestivalListResponseDTO> page = festivalService.getFestivals(pageable, all);
        return ApiResponseUtil.success(page, "페스티벌 목록 조회 성공");
    }


    @Operation(summary = "공연 상세 조회", description = "공연 ID(fid)로 상세 정보를 조회합니다.")
    @GetMapping("/{fid}")
    public ResponseEntity<SuccessResponse<FestivalDetailResponseDTO>> getFestivalDetail(
            @PathVariable("fid") String fid
    ) {
        FestivalDetailResponseDTO detail = festivalService.getFestivalDetail(fid);
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

    @Operation(summary = "공연 조회수 증가", description = "공연 ID(fid)로 조회수를 1 증가시킵니다. 쿠키를 이용하여 중복을 방지합니다.")
    @PostMapping("/views/{fid}")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> increaseViews(
            @PathVariable("fid") String fid,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1. 쿠키 확인
        Cookie[] cookies = request.getCookies();
        boolean alreadyViewed = false;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // "viewed_" + fid 쿠키가 있는지 확인
                if (("viewed_" + fid).equals(cookie.getName())) {
                    alreadyViewed = true;
                    break;
                }
            }
        }

        // 2. 조회수 증가 및 쿠키 발급
        if (!alreadyViewed) {
            int updatedViews = festivalService.increaseViews(fid);

            Cookie newCookie = new Cookie("viewed_" + fid, "true");
            newCookie.setMaxAge(60 * 60 * 24); // 24시간 동안 유효
            newCookie.setHttpOnly(true);       // JavaScript 접근 방지
            newCookie.setPath("/");            // 모든 경로에서 쿠키 유효
            response.addCookie(newCookie);

            Map<String, Object> result = Map.of("views", updatedViews);
            return ApiResponseUtil.success(result, "조회수 증가");
        }

        // 3. 이미 조회한 경우 현재 조회수만 반환
        int currentViews = festivalService.getViews(fid);
        Map<String, Object> result = Map.of("views", currentViews);
        return ApiResponseUtil.success(result, "이미 조회한 공연");
    }

    @PostMapping("/geocode")
    public ResponseEntity<SuccessResponse<Integer>> run(@RequestParam(defaultValue = "100") int size) {
        int success = festivalGeocodeService.geocodeBatch(size);
        return ApiResponseUtil.success(success, "성공");
    }

    @GetMapping("/test")
    public ResponseEntity<SuccessResponse<Integer>> run() {

        int a=0;
        for(int i=0; i < 100; i++){
           a++;
        }
        return ApiResponseUtil.success(0," success");
    }

}