package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.entity.Activity;
import com.teckit.festival.entity.Course;
import com.teckit.festival.enumeration.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "카카오 search keyword 응답 DTO", name = "KakaoResponseDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoResponseDTO {
    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("x")
    private String longitude;

    @JsonProperty("y")
    private String latitude;

    public static List<ActivityDTO> toActivityListDto(List<KakaoResponseDTO> activities, ActivityType activityType) {
        List<ActivityDTO> result = new ArrayList<>();

        for (KakaoResponseDTO restaurant : activities) {
            result.add(toActivityDto(restaurant, activityType));
        }

        return result;
    }

    public static ActivityDTO toActivityDto(KakaoResponseDTO responseDTO, ActivityType activityType) {
        return ActivityDTO.builder()
                .activityName(responseDTO.placeName)
                .addressName(responseDTO.getAddressName())
                .latitude(Double.parseDouble(responseDTO.getLatitude()))
                .longitude(Double.parseDouble(responseDTO.getLongitude()))
                .activityType(activityType)
                .build();
    }
}
