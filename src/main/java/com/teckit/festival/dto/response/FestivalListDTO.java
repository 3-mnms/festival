package com.teckit.festival.dto.response;

import com.teckit.festival.dto.response.FestivalListItemDTO;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;

import java.util.List;

@Getter
@XmlRootElement(name = "dbs")
@XmlAccessorType(XmlAccessType.FIELD)
public class FestivalListDTO {
    @XmlElement(name = "db")
    private List<FestivalListItemDTO> festivalList;
}