package com.teckit.festival.dto;

import com.teckit.festival.dto.response.NearbyFestivalDTO;

import java.time.LocalDate;

public interface NearbyFestivalInterface {
    String getFestivalDetailId();
    String getName();
    String getFestivalCast();
    String getAddress();
    Double getLatitude();
    Double getLongitude();
    LocalDate getFinishDate();
    Double getDistance();
    String getPoster();

    default NearbyFestivalDTO toDto() {
        return NearbyFestivalDTO.builder()
                .festivalDetailId(getFestivalDetailId())
                .name(getName())
                .cast(getFestivalCast())
                .address(getAddress())
                .latitude(getLatitude())
                .longitude(getLongitude())
                .finishDate(getFinishDate())
                .distance(getDistance())
                .poster(getPoster())
                .build();
    }
}
