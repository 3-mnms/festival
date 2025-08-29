package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "페스티벌 기대평 AI 분석 응답 DTO", name = "ReviewAnalyzeResponseDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewAnalyzeResponseDTO {
    @JsonProperty("summary")
    @Schema(description = "새로운 기대평 요약 내용")
    private String analyzeContent;

    @JsonProperty("emotion")
    @Schema(description = "새로운 기대평 감정 분석")
    private String emotion;

    @JsonProperty("positive_count")
    @Schema(description = "긍정 감정 개수")
    private int positiveCount;

    @JsonProperty("negative_count")
    @Schema(description = "부정 감정 개수")
    private int negativeCount;

    @JsonProperty("neutral_count")
    @Schema(description = "중립 감정 개수")
    private int neutralCount;
}
