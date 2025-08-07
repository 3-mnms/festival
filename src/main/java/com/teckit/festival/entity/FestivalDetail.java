package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

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
    private String id;  // 예: PF000001

    @OneToOne(mappedBy = "festivalDetail", cascade = CascadeType.ALL)
    @JsonBackReference
    private Festival festival;

    @Column(nullable = false)
    private String loginId;

    @Column
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
    private int prfage;
    private String posterFile;

    @ElementCollection
    private List<String> contentFile;

    @OneToMany(mappedBy = "festivalDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FestivalSchedule> schedules;
}