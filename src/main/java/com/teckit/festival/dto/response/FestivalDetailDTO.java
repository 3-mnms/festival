package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FestivalDetailDTO {

    // ✅ XML에서 매핑되는 필드
    @XmlElement private String mt20id;
    @XmlElement private String mt10id;
    @XmlElement private String prfnm;
    @XmlElement private String prfpdfrom;
    @XmlElement private String prfpdto;
    @XmlElement private String fcltynm;
    @XmlElement private String prfcast;
    @XmlElement private String prfruntime;
    @XmlElement private String prfage;
    @XmlElement private String pcseguidance;
    @XmlElement private String poster;
    @XmlElement private String sty;
    @XmlElement private String genrenm;
    @XmlElement private String prfstate;
    @XmlElement private String updatedate;
    @XmlElement private List<String> styurls;

    // ✅ 클라이언트 입력값
    private String loginId;
    private String faddress;

    @Min(0)
    @Max(2)
    private int ticketPick;
    private int maxPurchase;

    public FestivalDetail toEntity(int ticketPrice, int availableNOP) {
        return FestivalDetail.builder()
                .id(this.mt20id) // PF000001
                .loginId(loginId)
                .availableNOP(availableNOP)
                .fcltyid(mt10id)
                .fname(prfnm)
                .fdfrom(prfpdfrom)
                .fdto(prfpdto)
                .fcltynm(fcltynm)
                .fcast(prfcast)
                .story(sty)
                .ticketPrice(ticketPrice)
                .genrenm(genrenm)
                .fstate(prfstate)
                .updatedate(updatedate)
                .faddress(faddress)
                .ticketPick(ticketPick)
                .maxPurchase(maxPurchase)
                .prfage(Integer.parseInt(prfage))
                .posterFile(poster)
                .contentFile(styurls)
                .views(0)
                .build();
    }

    public static FestivalDTO fromEntity(Festival festival) {
        return FestivalDTO.builder()
                .mt20id(festival.getFestivalDetail().getId()) // fid
                .prfnm(festival.getFname())
                .prfpdfrom(festival.getFdfrom().toString())
                .prfpdto(festival.getFdto().toString())
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
