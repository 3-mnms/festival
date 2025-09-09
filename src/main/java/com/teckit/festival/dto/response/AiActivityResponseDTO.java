package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.entity.Activity;
import com.teckit.festival.entity.Course;
import com.teckit.festival.entity.NearbyFestival;
import com.teckit.festival.enumeration.ActivityType;
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

    public static Course convertToCourse(AiActivityResponseDTO dto, NearbyFestival nearbyFestival) {
        return Course.builder()
                .course1(dto.getCourse1())
                .course2(dto.getCourse2())
                .course3(dto.getCourse3())
                .nearbyFestival(nearbyFestival)
                .build();
    }

    public static List<Activity> convertToActivity(AiActivityResponseDTO dto, NearbyFestival nearbyFestival) {
        List<Activity> result = new ArrayList<>();

        for (ActivityPlaceDTO restaurant : dto.getRestaurants()) {
            result.add(toActivityEntity(restaurant, nearbyFestival, ActivityType.Restaurant));
        }

        for (ActivityPlaceDTO hotPlace : dto.getHotPlaces()) {
            result.add(toActivityEntity(hotPlace, nearbyFestival, ActivityType.HotPlace));
        }

        return result;
    }

    public static Activity toActivityEntity(ActivityPlaceDTO dto, NearbyFestival nearbyFestival, ActivityType type) {
        return Activity.builder()
                .activityName(dto.getPlaceName())
                .addressName(dto.getAddressName())
                .latitude(Double.valueOf(dto.getLatitude()))
                .longitude(Double.valueOf(dto.getLongitude()))
                .activityType(type)
                .nearbyFestival(nearbyFestival)
                .build();
    }
}
