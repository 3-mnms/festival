package com.teckit.festival.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class FestivalKafkaDTO {
    private String id;           // PF000001
    private String fname;
    private String fdfrom;
    private String fdto;
    private String posterFile;
    private String fcltynm;
    private int ticketPick;
    private int maxPurchase;
    private int ticketPrice;
    private int availableNOP;
    private List<ScheduleDTO> schedules; // 문자열 대신 객체

    @Getter @Setter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ScheduleDTO {
        private Long scheduleId;   // festival_schedule PK
        private String dayOfWeek;  // "MON"
        private String time;       // "12:00"
    }
}
