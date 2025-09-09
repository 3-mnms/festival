package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.enumeration.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "맛집 혹은 놀거리 장소 DTO", name = "ActivityPlaceDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityPlaceDTO {
    @JsonProperty("place_name")
    private String placeName;

    @JsonProperty("address_name")
    private String addressName;

    @JsonProperty("x")
    private String longitude;

    @JsonProperty("y")
    private String latitude;
}