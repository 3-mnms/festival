package com.teckit.festival.dto;

import com.teckit.festival.dto.response.NearbyFestivalDTO;

import java.time.LocalDate;

public interface NearbyFestivalInterface {
    String getName();
    Double getLatitude();
    Double getLongitude();
    LocalDate getFinishDate();
    Double getDistance();

    default NearbyFestivalDTO toDto() {
        return NearbyFestivalDTO.builder()
                .name(getName())
                .latitude(getLatitude())
                .longitude(getLongitude())
                .finishDate(getFinishDate())
                .distance(getDistance())
                .build();
    }
}
