package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.dto.ActivityPlaceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "공연장 근처 맛집, 놀거리 AI 응답 DTO", name = "AiActivityResponseDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiActivityResponseDTO {

    @JsonProperty("restaurants")
    @Schema(description = "맛집 리스트 3개")
    private List<ActivityPlaceDTO> restaurants;

    @JsonProperty("hot_places")
    @Schema(description = "놀거리 리스트 3개")
    private List<ActivityPlaceDTO> hotPlaces;

    @JsonProperty("course1")
    @Schema(description = "코스 1")
    private String course1;

    @JsonProperty("course2")
    @Schema(description = "코스 2")
    private String course2;

    @JsonProperty("course3")
    @Schema(description = "코스 3")
    private String course3;

}
