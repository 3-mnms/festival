package com.teckit.festival.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FavoriteToggleResponse {
    private final boolean liked; // true=찜됨, false=해제
    private final long count;    // 해당 fid의 총 찜 수
}