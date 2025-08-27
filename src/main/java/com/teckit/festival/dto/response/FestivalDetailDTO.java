package com.teckit.festival.dto.response;

import com.teckit.festival.dto.FestivalKafkaDTO;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.util.DateUtil;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.xml.bind.annotation.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FestivalDetailDTO {

    @XmlElement(name = "mt20id")
    private String id;

    @XmlElement(name = "mt10id")
    private String fcltyid;

    @XmlElement(name = "prfnm")
    private String fname;

    @XmlElement(name = "prfpdfrom")
    private String fdfrom;

    @XmlElement(name = "prfpdto")
    private String fdto;

    @XmlElement(name = "fcltynm")
    private String fcltynm;

    @XmlElement(name = "prfcast")
    private String fcast;

    @XmlElement(name = "prfage")
    private String prfage;

    @XmlElement(name = "poster")
    private String posterFile;

    @XmlElement(name = "sty")
    private String story;

    @XmlElement(name = "genrenm")
    private String genrenm;

    @XmlElement(name = "prfstate")
    private String fstate;

    @XmlElement(name = "updatedate")
    private String updatedate;

    @XmlElement(name = "entrpsnmH")
    private String entrpsnmH;

    @XmlElement(name = "prfruntime")
    private String runningTime;

    @Builder.Default
    @XmlElementWrapper(name = "styurls")
    @XmlElement(name = "styurl")
    private List<String> contentFile = new ArrayList<>();

    // 내부 전용 필드
    private Long userId;
    private String faddress;
    @Min(1) @Max(3)
    private int ticketPick;
    @Min(1) @Max(4)
    private int maxPurchase;
    private int availableNOP;
    private int ticketPrice;

    public FestivalDetail toEntity(int ticketPrice, int availableNOP) {
        // 기본값 보정
        int safeTicketPick   = (this.ticketPick   <= 0) ? 1 : this.ticketPick;
        int safeMaxPurchase  = (this.maxPurchase  <= 0) ? 1 : this.maxPurchase;

        int finalAvailableNOP = Math.max(0, availableNOP);
        int finalTicketPrice  = ticketPrice;

        LocalDateTime updatedDateTime = null;
        if (this.updatedate != null && !this.updatedate.isBlank()) {
            try {
                // 소수점 이하 초를 처리하기 위한 패턴으로 변경
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]");
                updatedDateTime = LocalDateTime.parse(this.updatedate, formatter);
            } catch (Exception e) {
                // 파싱 실패 시 예외 처리
                System.err.println("updatedate 파싱 실패: " + this.updatedate + " - " + e.getMessage());
                updatedDateTime = LocalDateTime.now();
            }
        } else {
            updatedDateTime = LocalDateTime.now();
        }

        return FestivalDetail.builder()
                .id(id)
                .userId(this.userId != null ? this.userId : 0)
                .fcltyid(fcltyid)
                .fname(fname)
                .fdfrom(DateUtil.parseDate(this.fdfrom))
                .fdto(DateUtil.parseDate(this.fdto))
                .fcltynm(fcltynm)
                .fcast(fcast)
                .prfage(this.prfage)
                .story(story)
                .ticketPrice(finalTicketPrice)
                .availableNOP(finalAvailableNOP)
                .genrenm(genrenm)
                .fstate(fstate)
                .updatedate(updatedDateTime)
                .faddress(faddress)
                .ticketPick(safeTicketPick)
                .maxPurchase(safeMaxPurchase)
                .posterFile(posterFile)
                .contentFile(contentFile != null ? contentFile : new ArrayList<>())
                .views(0)
                .entrpsnmH(entrpsnmH)
                .runningTime(runningTime)
                .build();
    }

    public static FestivalDetailDTO fromEntity(FestivalDetail entity) {
        return FestivalDetailDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .fcltyid(entity.getFcltyid())
                .fname(entity.getFname())
                .fdfrom(DateUtil.formatDate(entity.getFdfrom()))
                .fdto(DateUtil.formatDate(entity.getFdto()))
                .fcltynm(entity.getFcltynm())
                .fcast(entity.getFcast())
                .prfage(entity.getPrfage())
                .story(entity.getStory())
                .ticketPrice(entity.getTicketPrice())
                .availableNOP(entity.getAvailableNOP())
                .genrenm(entity.getGenrenm())
                .fstate(entity.getFstate())
                .updatedate(entity.getUpdatedate() != null ? entity.getUpdatedate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                .faddress(entity.getFaddress())
                .ticketPick(entity.getTicketPick())
                .maxPurchase(entity.getMaxPurchase())
                .posterFile(entity.getPosterFile())
                .contentFile(entity.getContentFile())
                .entrpsnmH(entity.getEntrpsnmH())
                .runningTime(entity.getRunningTime())
                .build();
    }

    public String getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(String updatedate) {
        this.updatedate = updatedate;
    }
}
