package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    private String fdfrom;   // 문자열 그대로
    private String fdto;     // 문자열 그대로
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

    @ElementCollection
    private List<String> contentFile;

    @Builder.Default
    @OneToMany(mappedBy = "festivalDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FestivalSchedule> schedules = new ArrayList<>();

    /** 부모-자식 연관관계 주입 포함해서 안전하게 교체 */
    public void setSchedules(List<FestivalSchedule> schedules) {
        if (this.schedules == null) {
            this.schedules = new ArrayList<>();
        } else {
            this.schedules.clear();
        }
        if (schedules != null) {
            for (FestivalSchedule s : schedules) {
                s.setFestivalDetail(this);
            }
            this.schedules.addAll(schedules);
        }
    }
}
