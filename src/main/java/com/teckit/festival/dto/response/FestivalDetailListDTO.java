package com.teckit.festival.dto.response;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "dbs")
@XmlAccessorType(XmlAccessType.FIELD)
public class FestivalDetailListDTO {
    @XmlElement(name="db")
    List<FestivalDetailDTO> festivalDetailList;
}
