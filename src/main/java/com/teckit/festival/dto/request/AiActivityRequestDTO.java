package com.teckit.festival.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.dto.response.KakaoResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "공연장 근처 맛집, 놀거리 AI 요청 DTO", name = "AiActivityRequestDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiActivityRequestDTO {
    @JsonProperty("restaurants")
    @Schema(description = "맛집 리스트")
    private List<KakaoResponseDTO> restaurants;

    @JsonProperty("hot_places")
    @Schema(description = "놀거리 리스트")
    private List<KakaoResponseDTO> hotPlaces;
}
