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
    private String posterFile;    // 대표 이미지(썸네일) - Festival에서 가져올 수 있음
    private String fcltynm;
    private String genrenm;
    private String fstate;
    private String faddress;
    private String prfage;
    private int ticketPick;
    private int maxPurchase;
    private int ticketPrice;
    private int availableNOP;
    private List<String> schedules; // "MON 19:30" 같은 문자열 리스트(선택)
    private String loginId;       // 주최자
}
