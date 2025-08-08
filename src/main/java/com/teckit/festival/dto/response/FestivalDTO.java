package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalDTO {
    private String mt20id;
    private String prfnm;
    private String prfpdfrom;
    private String prfpdto;
    private String fcltynm;
    private String poster;
    private String area;
    private String genrenm;
    private String prfstate;
    private String prfage;
    private String loginId;
    private int ticketPick;
    private int maxPurchase;
    private int ticketPrice;
    private int availableNOP;

    public static FestivalDTO fromEntity(Festival festival) {
        return FestivalDTO.builder()
                .mt20id(festival.getFestivalDetail().getId())
                .prfnm(festival.getFname())
                .prfpdfrom(festival.getFdfrom())
                .prfpdto(festival.getFdto())
                .fcltynm(festival.getFcltynm())
                .poster(festival.getPosterFile())
                .area(festival.getArea())
                .genrenm(festival.getGenrenm())
                .prfstate(festival.getFstate())
                .prfage(festival.getFage())
                .loginId(festival.getLoginId())
                .ticketPick(festival.getTicketPick())
                .maxPurchase(festival.getMaxPurchase())
                .ticketPrice(festival.getTicketPrice())
                .availableNOP(festival.getAvailableNOP())
                .build();
    }
}
