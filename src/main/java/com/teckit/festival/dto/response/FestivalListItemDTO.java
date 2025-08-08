package com.teckit.festival.dto.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FestivalListItemDTO {

    @XmlElement(name = "mt20id")
    private String mt20id; // 공연 ID

    @XmlElement(name = "prfnm")
    private String prfnm; // 공연명

    @XmlElement(name = "prfpdfrom")
    private String prfpdfrom; // 공연 시작일

    @XmlElement(name = "prfpdto")
    private String prfpdto; // 공연 종료일

    @XmlElement(name = "fcltynm")
    private String fcltynm; // 공연시설명

    @XmlElement(name = "poster")
    private String poster; // 포스터 이미지

    @XmlElement(name = "area")
    private String area; // 지역

    @XmlElement(name = "genrenm")
    private String genrenm; // 장르

    @XmlElement(name = "prfstate")
    private String prfstate; // 상태
}
