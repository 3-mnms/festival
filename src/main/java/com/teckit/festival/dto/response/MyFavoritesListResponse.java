// 사용자 별 관심 상품 조회용 응답 DTO
package com.teckit.festival.dto.response;

import com.teckit.festival.dto.request.MyFavoritesDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyFavoritesListResponse {
    private final List<MyFavoritesDTO> items;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;
}