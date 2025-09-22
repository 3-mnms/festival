package com.teckit.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "공연장 근처 맛집, 놀거리 응답 DTO", name = "RecommendDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendDTO {
    @Schema(description = "페스티벌 detailId")
    private String festivalDetailId;

    @Schema(description = "맛집 리스트 5개")
    private List<ActivityDTO> restaurants;

    @Schema(description = "놀거리 리스트 5개")
    private List<ActivityDTO> hotPlaces;

    @Schema(description = "코스")
    private CourseDTO courseDTO;

    public static RecommendDTO toDto(String festivalDetailId, List<ActivityDTO> restaurants, List<ActivityDTO> hotPlaces, CourseDTO courseDTO) {
        return RecommendDTO.builder()
                .festivalDetailId(festivalDetailId)
                .restaurants(restaurants)
                .hotPlaces(hotPlaces)
                .courseDTO(courseDTO)
                .build();
    }
}
