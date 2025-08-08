package com.teckit.festival.dto.response;

import com.teckit.festival.dto.FestivalKafkaDTO;
import com.teckit.festival.entity.FestivalDetail;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FestivalDetailDTO {

    @XmlElement(name = "mt20id")
    private String mt20id;

    @XmlElement(name = "mt10id")
    private String mt10id;

    @XmlElement(name = "prfnm")
    private String prfnm;

    @XmlElement(name = "prfpdfrom")
    private String prfpdfrom;

    @XmlElement(name = "prfpdto")
    private String prfpdto;

    @XmlElement(name = "fcltynm")
    private String fcltynm;

    @XmlElement(name = "prfcast")
    private String prfcast;

    @XmlElement(name = "prfcrew")
    private String prfcrew;

    @XmlElement(name = "prfruntime")
    private String prfruntime;

    @XmlElement(name = "prfage")
    private String prfage;

    @XmlElement(name = "pcseguidance")
    private String pcseguidance;

    @XmlElement(name = "poster")
    private String poster;

    @XmlElement(name = "sty")
    private String sty;

    @XmlElement(name = "genrenm")
    private String genrenm;

    @XmlElement(name = "prfstate")
    private String prfstate;

    @XmlElement(name = "updatedate")
    private String updatedate;

    @XmlElementWrapper(name = "styurls")
    @XmlElement(name = "styurl")
    private List<String> styurls;

    // 내부 전용 필드
    private String loginId;
    private String faddress;
    @Min(1) @Max(3)
    private int ticketPick;
    @Min(1) @Max(4)
    private int maxPurchase;
    private int availableNOP;
    private int ticketPrice;

    public FestivalDetail toEntity(int ticketPrice, int availableNOP) {
        // 기본값 보정
        int safeTicketPick   = (this.ticketPick   <= 0) ? 1 : this.ticketPick;
        int safeMaxPurchase  = (this.maxPurchase  <= 0) ? 1 : this.maxPurchase;
        int safeAvailableNOP = (this.availableNOP <  0) ? 0 : this.availableNOP;

        int finalAvailableNOP = Math.max(0, availableNOP);
        int finalTicketPrice  = ticketPrice;

        return FestivalDetail.builder()
                .id(mt20id)
                .loginId(this.loginId != null ? this.loginId : "SYSTEM")
                .fcltyid(mt10id)
                .fname(prfnm)
                .fdfrom(prfpdfrom)
                .fdto(prfpdto)
                .fcltynm(fcltynm)
                .fcast(prfcast)
                .prfage(this.prfage)
                .story(sty)
                .ticketPrice(finalTicketPrice)
                .availableNOP(finalAvailableNOP)
                .genrenm(genrenm)
                .fstate(prfstate)
                .updatedate(updatedate)
                .faddress(faddress)
                .ticketPick(safeTicketPick)
                .maxPurchase(safeMaxPurchase)
                .posterFile(poster)
                .contentFile(styurls != null ? styurls : new ArrayList<>())
                .views(0)
                .build();
    }
}
