package com.teckit.festival.dto.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

import java.util.List;

@XmlRootElement(name = "dbs")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class FestivalListDTO {
    @XmlElement(name = "db")
    private List<FestivalDTO> festivalList;
}