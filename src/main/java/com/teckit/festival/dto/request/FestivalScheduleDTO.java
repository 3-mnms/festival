package com.teckit.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공연 일정 DTO")
public class FestivalScheduleDTO {

    @Schema(description = "공연 상세정보 ID", example = "1")
    private String festivalDetailId;

    @Schema(description = "요일 (MON, TUE, WED, THU, FRI, SAT, SUN, HOL)", example = "MON")
    private String dayOfWeek;

    @Schema(description = "공연 시간", example = "18:00")
    private String time;
}
