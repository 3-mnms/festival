package com.teckit.festival.dto.response;

import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.NearbyFestival;
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
    @Schema(description = "festivalDetail id")
    private String festivalDetailId;

    @Schema(description = "공연명")
    private String name;

    @Schema(description = "출연자")
    private String cast;

    @Schema(description = "공연장 주소")
    private String address;

    @Schema(description = "공연장 주소 위도")
    private Double latitude; //위도

    @Schema(description = "공연장 주소 경도")
    private Double longitude;//경도

    @Schema(description = "공연 마감 날짜")
    private LocalDate finishDate;

    @Schema(description = "사용자 주소 - 페스티벌 공연장 거리")
    private Double distance;

    @Schema(description = "공연 포스터")
    private String poster;

    @Schema(hidden = true)
    public static NearbyFestivalDTO fromEntity(NearbyFestival nearbyFestival) {
        FestivalDetail festivalDetail = nearbyFestival.getFestivalDetail();

        return NearbyFestivalDTO.builder()
                .festivalDetailId(festivalDetail.getId())
                .name(festivalDetail.getFname())
                .cast(festivalDetail.getFcast())
                .address(festivalDetail.getFaddress())
                .latitude(nearbyFestival.getLatitude())
                .longitude(nearbyFestival.getLongitude())
                .finishDate(festivalDetail.getFdto())
                .distance(nearbyFestival.getDistance())
                .poster(festivalDetail.getPosterFile())
                .build();
    }

    @Schema(hidden = true)
    public NearbyFestival toEntity(Long userId, FestivalDetail festivalDetail) {

        return NearbyFestival.builder()
                .userId(userId)
                .latitude(latitude)
                .longitude(longitude)
                .finishDate(finishDate)
                .distance(distance)
                .festivalDetail(festivalDetail)
                .build();
    }

}
