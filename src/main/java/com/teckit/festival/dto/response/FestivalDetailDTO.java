package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class FestivalDetailDTO {
    private String mt20id; // 공연 ID
    private String mt10id; // 공연시설 ID
    private String prfnm; // 공연명
    private String prfpdfrom; // 공연 시작일
    private String prfpdto; // 공연 종료일
    private String fcltynm; // 공연시설명
    private String prfcast; // 출연진
    private String prfcrew; // 제작진
    private String prfruntime; // 런타임
    private String prfage; // 관람 연령
    private String entrpsnmP; // 제작사
    private String entrpsnmA; // 기획사
    private String entrpsnmH; // 주최
    private String entrpsnmS; // 주관
    private String pcseguidance; // 티켓 가격
    private String posterFile; // 포스터 이미지
    private String sty; // 줄거리
    private String genrenm; // 공연 장르
    private String prfstate; // 공연 상태
    private String openrun; // 오픈런 여부
    private String visit; // 내한 여부
    private String child; // 아동극 여부
    private String daehakro; // 대학로 여부
    private String isFestival; // 축제 여부
    private String musicallicense; // 뮤지컬 라이선스 여부
    private String musicalcreate; // 뮤지컬 창작 여부
    private String updatedate; // 최종 수정일

    private String faddress; // 공연장 주소

    @Min(0)
    @Max(2)
    private int ticketPick; // 티켓 수령 방식 (0, 1, 2)

    private int maxPurchase; // 구매 제한

    private List<String> contentFile; // 상세 이미지 리스트

    public FestivalDetail toEntity(Festival festival, int ticketPrice, int availableNOP) {
        return FestivalDetail.builder()
                .festival(festival)
                .availableNOP(availableNOP)
                .fcltyid(mt10id)
                .fname(prfnm)
                .fdfrom(prfpdfrom)
                .fdto(prfpdto)
                .fcltynm(fcltynm)
                .fcast(prfcast)
                .fage(prfage)
                .ticketPick(ticketPick)
                .maxPurchase(maxPurchase)
                .ticketPrice(ticketPrice)
                .posterFile(posterFile)
                .story(sty)
                .genrenm(genrenm)
                .fstate(prfstate)
                .visit(visit)
                .updatedate(updatedate)
                .faddress(faddress)
                .contentFile(contentFile)
                .views(0)
                .build();
    }
}
