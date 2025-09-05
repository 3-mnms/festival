package com.teckit.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "사용자 주소 근처 페스티벌 조회 DTO(단건)", name = "NearbyFestivalDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyFestivalDTO {

    @Schema(description = "공연명")
    private String name;

    @Schema(description = "사용자 주소 위도")
    private Double latitude; //위도

    @Schema(description = "사용자 주소 경도")
    private Double longitude;//경도

    @Schema(description = "공연 마감 날짜")
    private LocalDate finishDate;

    @Schema(description = "사용자 주소 - 페스티벌 공연장 거리")
    private Double distance;

}
