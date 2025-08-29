package com.teckit.festival.dto.response;

import com.teckit.festival.entity.FestivalReview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "페스티벌 기대평 응답 DTO", name = "FestivalReviewResponseDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalReviewResponseDTO {
    @Schema(description = "기대평 내용")
    private String reviewContent;

    @Schema(description = "사용자 userId")
    private Long userId;

    @Schema(description = "기대평 생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "기대평 수정 시간")
    private LocalDateTime updatedAt;

    @Schema(hidden = true)
    public static FestivalReviewResponseDTO fromEntity(FestivalReview festivalReview) {
        return FestivalReviewResponseDTO.builder()
                .reviewContent(festivalReview.getReviewContent())
                .userId(festivalReview.getUserId())
                .createdAt(festivalReview.getCreatedAt())
                .updatedAt(festivalReview.getUpdatedAt())
                .build();
    }
}
