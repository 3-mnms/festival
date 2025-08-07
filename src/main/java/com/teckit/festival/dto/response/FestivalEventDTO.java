package com.teckit.festival.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalEventDTO {
    private String festivalId;
    private String eventType;     // CREATED / UPDATED / DELETED
    private String description;
}