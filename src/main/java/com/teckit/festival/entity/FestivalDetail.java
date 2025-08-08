package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.teckit.festival.dto.FestivalKafkaDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FestivalDetail {

    @Id
    @Column(length = 20)
    private String id;  // 예: PF132236

    @OneToOne(mappedBy = "festivalDetail", cascade = CascadeType.ALL)
    @JsonBackReference
    private Festival festival;

    @Column(nullable = false)
    private String loginId;

    private String fcltyid;
    private String fname;
    private String fdfrom;
    private String fdto;
    private String fcltynm;
    private String fcast;

    @Column(length = 1000)
    private String story;

    private int ticketPrice;
    private String genrenm;
    private String fstate;
    private String updatedate;
    private int availableNOP;
    private int views;
    private String faddress;
    private int ticketPick;
    private int maxPurchase;
    private String prfage;
    private String posterFile;

    @Builder.Default
    @ElementCollection
    private List<String> contentFile = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "festivalDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FestivalSchedule> schedules = new ArrayList<>();

    /** 부모-자식 연관관계 주입 포함해서 안전하게 교체 */
    public void setSchedules(List<FestivalSchedule> schedules) {
        this.schedules.clear();
        if (schedules != null) {
            for (FestivalSchedule s : schedules) {
                s.setFestivalDetail(this);
            }
            this.schedules.addAll(schedules);
        }
    }

    /** Kafka DTO 변환 메서드 - null 안전 처리 포함 */
    /** Kafka 전송용 변환 메서드 */
    public FestivalKafkaDTO toKafkaDTO() {
        List<String> scheduleStrings = (this.schedules == null) ? new ArrayList<>() :
                this.schedules.stream()
                        .map(s -> s.getDayOfWeek() + " " + s.getTime())
                        .collect(Collectors.toList());

        return FestivalKafkaDTO.builder()
                .id(this.id) //
                .loginId(this.loginId)
                /*.loginId(this.loginId != null ? this.loginId
                        : (this.festival != null ? this.festival.getLoginId() : null))*/
                /*.fname(this.fname != null ? this.fname
                        : (this.festival != null ? this.festival.getFname() : null))
                .fdfrom(this.fdfrom != null ? this.fdfrom
                        : (this.festival != null ? this.festival.getFdfrom() : null))
                .fdto(this.fdto != null ? this.fdto
                        : (this.festival != null ? this.festival.getFdto() : null))
                .posterFile(this.posterFile != null ? this.posterFile
                        : (this.festival != null ? this.festival.getPosterFile() : null))
                .fcltynm(this.fcltynm)
                .genrenm(this.genrenm != null ? this.genrenm
                        : (this.festival != null ? this.festival.getGenrenm() : null))*/
                .fname(this.fname)
                .fdfrom(this.fdfrom)
                .fdto(this.fdto)
                .posterFile(this.posterFile)
                .fcltynm(this.fcltynm)
                .genrenm(this.genrenm)
                .fstate(this.fstate)

                .faddress(this.faddress)
                .prfage(this.prfage)
                .ticketPick(this.ticketPick)
                .maxPurchase(this.maxPurchase)
                .ticketPrice(this.ticketPrice)
                .availableNOP(this.availableNOP)
                .schedules(scheduleStrings)
                .build();
    }
}