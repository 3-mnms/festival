package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class FestivalDTO {
//    공연 id
    @XmlElement private String mt20id;
//    공연명
    @XmlElement private String prfnm;
//    공연 시작일
    @XmlElement private String prfpdfrom;
//    공연 종료일
    @XmlElement private String prfpdto;
//    공연 시설명
    @XmlElement private String fcltynm;
//    포스터 이미지 경로
    @XmlElement private String poster;
//    공연지역
    @XmlElement private String area;
//    공연 장르명
    @XmlElement private String genrenm;
//    오픈런
    @XmlElement private String openrun;
//    공연 상태
    @XmlElement
    private String prfstate;

    public Festival toEntity() {
        return Festival.builder()
                .id(mt20id)
                .fname(prfnm)
                .fdfrom(prfpdfrom)
                .fdto(prfpdto)
                .poster(poster)
                .area(area)
                .fcltynm(fcltynm)
                .genrename(genrenm)
                .openrun(openrun)
                .fstate(prfstate)
                .build();
    }
}
