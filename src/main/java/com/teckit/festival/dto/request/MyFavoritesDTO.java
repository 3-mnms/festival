package com.teckit.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 프론트랑 논의 후 추후 수정 가능
@Getter
@AllArgsConstructor
public class MyFavoritesDTO {

    @Schema(description = "공연 식별 ID", example = "FB123456")
    private final String fid;

    @Schema(description = "공연 제목", example = "구름빵")
    private final String fname;

    @Schema(description = "포스터 이미지", example = "https://example.com/poster.jpg")
    private final String posterFile;
}