package com.teckit.festival.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "페스티벌 기대평 AI 분석 요청 DTO", name = "AiReviewRequestDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiReviewRequestDTO {
    @JsonProperty("summary")
    @Schema(description = "기대평 요약 내용")
    private String analyzeContent;

    @JsonProperty("new_review")
    @Schema(description = "새로운 기대평 내용")
    private String newContent;

    @JsonProperty("p_count")
    @Schema(description = "긍정 감정 개수")
    private int positiveCount;

    @JsonProperty("neg_count")
    @Schema(description = "부정 감정 개수")
    private int negativeCount;

    @JsonProperty("neu_count")
    @Schema(description = "중립 감정 개수")
    private int neutralCount;

    public static AiReviewRequestDTO from(String analyzeContent, String newContent, int positiveCount, int negativeCount, int neutralCount) {
        return AiReviewRequestDTO.builder()
                .analyzeContent(analyzeContent)
                .newContent(newContent)
                .positiveCount(positiveCount)
                .negativeCount(negativeCount)
                .neutralCount(neutralCount)
                .build();
    }
}
