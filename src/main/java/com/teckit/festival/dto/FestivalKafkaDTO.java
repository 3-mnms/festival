package com.teckit.festival.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class FestivalKafkaDTO {
    private String eventType;    // e.g. "FESTIVAL_CREATED" / "FESTIVAL_UPDATED" / "FESTIVAL_DELETED"
    private String id;           // PF000001
    private Long userId;
    private String fname;
    private LocalDate fdfrom;
    private LocalDate fdto;
    private String posterFile;
    private String fcltynm;
    private int ticketPick;
    private int maxPurchase;
    private int ticketPrice;
    private int availableNOP;
    private List<ScheduleDTO> schedules;

    @Getter @Setter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ScheduleDTO {
        private String dayOfWeek;  // "MON"
        private String time;       // "12:00"
    }
}
