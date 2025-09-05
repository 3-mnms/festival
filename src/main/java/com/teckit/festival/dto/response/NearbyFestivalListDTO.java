package com.teckit.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "사용자 주소 근처 페스티벌 조회 DTO(3개)", name = "NearbyFestivalListDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyFestivalListDTO {
    @Schema(description = "사용자 주소 정보(geocode)")
    UserGeocodeInfoDTO userGeocodeInfo;

    @Schema(description = "페스티벌 list")
    List<NearbyFestivalDTO> festivalList;
}
