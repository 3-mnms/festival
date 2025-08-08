package com.teckit.festival.dto.response;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import java.util.List;

@Getter
@XmlRootElement(name = "dbs")
@XmlAccessorType(XmlAccessType.FIELD)
public class FestivalDetailListDTO {
    @XmlElement(name = "db")
    private List<FestivalDetailDTO> festivalDetailList;
}
