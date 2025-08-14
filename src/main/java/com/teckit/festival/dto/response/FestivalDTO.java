package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalDTO {
    private String mt20id;       // FestivalDetail.id
    private String prfnm;        // 공연명
    private LocalDate prfpdfrom;    // 시작일
    private LocalDate prfpdto;      // 종료일
    private String fcltynm;      // 장소명
    private String poster;       // 썸네일
    //private String area;         // 지역
    private String genrenm;      // 장르
    private String prfstate;     // 상태
    private String prfage;       // 관람 연령
    private int ticketPick;      // 티켓 방식
    private int maxPurchase;     // 1인 최대 구매 수량
    private int ticketPrice;     // 티켓 가격
    //private int availableNOP;    // 수용 인원

    public static FestivalDTO fromEntity(Festival festival) {
        return FestivalDTO.builder()
                .mt20id(festival.getFestivalDetail().getId())
                .prfnm(festival.getFname())
                .prfpdfrom(festival.getFdfrom())
                .prfpdto(festival.getFdto())
                .fcltynm(festival.getFcltynm())
                .poster(festival.getPosterFile())
                //.area(festival.getArea())
                .genrenm(festival.getGenrenm())
                .prfstate(festival.getFstate())
                .prfage(festival.getPrfage())
                .ticketPick(festival.getFestivalDetail().getTicketPick())
                .maxPurchase(festival.getFestivalDetail().getMaxPurchase())
                .ticketPrice(festival.getFestivalDetail().getTicketPrice())
                //.availableNOP(festival.getAvailableNOP())
                .build();
    }
}
