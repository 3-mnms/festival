package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.entity.Activity;
import com.teckit.festival.enumeration.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "공연장 근처 놀거리, 맛집 조회 DTO(단건)", name = "ActivityDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDTO {
    @JsonProperty("activity_name")
    @Schema(description = "장소명")
    private String activityName;

    @JsonProperty("address_name")
    @Schema(description = "장소 주소")
    private String addressName;

    @JsonProperty("latitude")
    @Schema(description = "장소 위도")
    private Double latitude;

    @JsonProperty("longitude")
    @Schema(description = "장소 경도")
    private Double longitude;

    @JsonProperty("activity_type")
    @Schema(description = "장소 타입 (Restaurant or HotPlace)")
    private ActivityType activityType;

    public static List<ActivityDTO> toDtoList(List<Activity> activities) {
        List<ActivityDTO> result = new ArrayList<>();

        for (Activity activity : activities)
            result.add(toDto(activity));

        return result;
    }

    public static ActivityDTO toDto(Activity activity) {
        return ActivityDTO.builder()
                .activityName(activity.getActivityName())
                .addressName(activity.getAddressName())
                .latitude(activity.getLatitude())
                .longitude(activity.getLongitude())
                .activityType(activity.getActivityType())
                .build();
    }
}
