// 홈페이지 공연 목록 중 클릭 시 상세 정보 조회용 DTO
package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
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

    private List<String> times;
    private List<String> daysOfWeek;

    @Schema(description = "공연 관심 상품 등록 집계값")
    private long favoriteCount;

    @Schema(description = "관심 상품 사용자별 등록 상태 (등록 - true / 미등록 - false")
    private boolean favorited;

    public static FestivalDetailResponseDTO of(
            Festival f,
            FestivalDetail d,
            List<String> styurls,
            List<FestivalSchedule> schedules
    ) {
        return FestivalDetailResponseDTO.builder()
                .fid(d.getId())
                .prfnm(f.getFname())
                .prfpdfrom(f.getFdfrom())
                .prfpdto(f.getFdto())
                .fcltynm(f.getFcltynm())
                .fcast(d.getFcast())
                .prfage(f.getPrfage())
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
                .times(
                        schedules.stream()
                                .map(FestivalSchedule::getTime)
                                .toList()
                )
                .daysOfWeek(
                        schedules.stream()
                                .map(s -> {
                                    var dow = s.getDayOfWeek();     // FestivalScheduleDay enum
                                    return (dow == null) ? null : dow.name(); // "MON","TUE"...
                                })
                                .toList()
                )
                .build();
    }
}
