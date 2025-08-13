package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalDetailResponse {
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
    private String poster;
    private List<String> contentFiles;
    //private Integer views;
    //private String hostName;
    //private Integer likeCount;

    public static FestivalDetailResponse of(Festival f, FestivalDetail d, List<String> styurls) { // **
        return FestivalDetailResponse.builder()
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
                //.views(d.getViews() == null ? 0 : d.getViews())
                //.hostName(null)
                //.likeCount(null)
                .build();
    }
}
