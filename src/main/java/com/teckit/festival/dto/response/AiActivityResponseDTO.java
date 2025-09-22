package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.entity.Activity;
import com.teckit.festival.entity.Course;
import com.teckit.festival.entity.FestivalDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "공연장 근처 맛집, 놀거리 AI 응답 DTO", name = "AiActivityResponseDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiActivityResponseDTO {

    @JsonProperty("restaurants")
    @Schema(description = "맛집 리스트 5개")
    private List<ActivityDTO> restaurants;

    @JsonProperty("hot_places")
    @Schema(description = "놀거리 리스트 5개")
    private List<ActivityDTO> hotPlaces;

    @JsonProperty("course1")
    @Schema(description = "코스 1")
    private String course1;

    @JsonProperty("course2")
    @Schema(description = "코스 2")
    private String course2;

    @JsonProperty("course3")
    @Schema(description = "코스 3")
    private String course3;

    @JsonProperty("course4")
    @Schema(description = "코스 4")
    private String course4;

    @JsonProperty("course5")
    @Schema(description = "코스 5")
    private String course5;

    public static Course convertToCourse(AiActivityResponseDTO dto, FestivalDetail festivalDetail) {
        return Course.builder()
                .course1(dto.getCourse1())
                .course2(dto.getCourse2())
                .course3(dto.getCourse3())
                .course4(dto.getCourse4())
                .course5(dto.getCourse5())
                .festivalDetail(festivalDetail)
                .build();
    }

    public static List<Activity> convertToActivity(AiActivityResponseDTO dto, FestivalDetail festivalDetail) {
        List<Activity> result = new ArrayList<>();

        for (ActivityDTO restaurant : dto.getRestaurants()) {
            result.add(toActivityEntity(restaurant, festivalDetail));
        }

        for (ActivityDTO hotPlace : dto.getHotPlaces()) {
            result.add(toActivityEntity(hotPlace, festivalDetail));
        }

        return result;
    }

    public static Activity toActivityEntity(ActivityDTO dto, FestivalDetail festivalDetail) {
        return Activity.builder()
                .activityName(dto.getActivityName())
                .addressName(dto.getAddressName())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .activityType(dto.getActivityType())
                .festivalDetail(festivalDetail)
                .build();
    }
}
