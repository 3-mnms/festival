package com.teckit.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Schema(description = "페스티벌 기대평 응답 최종 DTO(페스티벌 기대평 전체+ 분석)", name = "FestivalReviewResultDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalReviewResultDTO {
    private Page<FestivalReviewResponseDTO> reviews;
    private ReviewAnalyzeResponseDTO analyze;
}
