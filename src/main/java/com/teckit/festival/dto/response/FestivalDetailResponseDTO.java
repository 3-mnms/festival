package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalDetailResponseDTO {
    private String fid;
    private String prfnm;
    private LocalDate prfpdfrom;
    private LocalDate prfpdto;
    private String fcltynm;
    private String fcast;
    private String prfage;
    private String story;
    private Integer ticketPrice;
    private Integer availableNOP;
    private String genrenm;
    private String prfstate;
    private String faddress;
    private Integer maxPurchase;

    @JsonProperty("entrpsnmH")
    private String entrpsnmH;

    @JsonProperty("runningtime")
    private String runningTime;

    private String poster;
    private List<String> contentFiles;
    //private Integer views;
    //private String hostName;
    //private Integer likeCount;

    public static FestivalDetailResponseDTO of(Festival f, FestivalDetail d, List<String> styurls) { // **
        return FestivalDetailResponseDTO.builder()
                .fid(d.getId())
                .prfnm(f.getFname())
                .prfpdfrom(f.getFdfrom())
                .prfpdto(f.getFdto())
                .fcltynm(f.getFcltynm())
                .fcast(d.getFcast())
                .prfage(f.getFage())
                .story(d.getStory())
                .ticketPrice(d.getTicketPrice())
                .availableNOP(d.getAvailableNOP())
                .genrenm(f.getGenrenm())
                .prfstate(f.getFstate())
                .faddress(d.getFaddress())
                .maxPurchase(d.getMaxPurchase())
                .poster(f.getPosterFile())
                .contentFiles(styurls)
                .entrpsnmH(d.getEntrpsnmH())
                .runningTime(d.getRunningTime())
                //.views(d.getViews() == null ? 0 : d.getViews())
                //.hostName(null)
                //.likeCount(null)
                .build();
    }
}
