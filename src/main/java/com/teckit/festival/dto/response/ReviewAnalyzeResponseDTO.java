package com.teckit.festival.dto.response;

import com.teckit.festival.entity.FestivalReviewAnalyze;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "페스티벌 기대평 분석 조회 DTO", name = "ReviewAnalyzeResponseDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAnalyzeResponseDTO {
    @Schema(description = "기대평 분석 Id")
    private Long analyzeId;

    @Schema(description = "기대평 분석 내용")
    private String analyzeContent;

    @Schema(description = "기대평 분석 긍정 퍼센트")
    private double positive;

    @Schema(description = "기대평 분석 부정 퍼센트")
    private double negative;

    @Schema(description = "기대평 분석 중립 퍼센트")
    private double neutral;

    @Schema(hidden = true)
    public static ReviewAnalyzeResponseDTO fromEntity(FestivalReviewAnalyze festivalReviewAnalyze) {
        return ReviewAnalyzeResponseDTO.builder()
                .analyzeId(festivalReviewAnalyze.getAnalyzeId())
                .analyzeContent(festivalReviewAnalyze.getAnalyzeContent())
                .positive(festivalReviewAnalyze.getPositive())
                .negative(festivalReviewAnalyze.getNegative())
                .neutral(festivalReviewAnalyze.getNeutral())
                .build();
    }
}
