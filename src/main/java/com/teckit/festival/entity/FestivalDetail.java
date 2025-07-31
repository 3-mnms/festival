package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "festival_detail")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "fid")
    @JsonManagedReference
    private Festival festival;

    private String fcltyid;
    private String fname;
    private String fdfrom;
    private String fdto;
    private String fcltynm;
    private String fcast;
    private String fcrew;
    private String fruntime;
    private String fage;
    private String entrpsnmP;
    private String entrpsnmA;
    private String entrpsnmH;
    private String entrpsnmS;

    @Column(nullable = false)
    private int ticketPrice;

    @Column(length = 1000)
    private String poster;

    @Lob
    private String story;

    private String genrenm;
    private String fstate;
    private String openrun;
    private String visit;
    private String child;
//    수용 가능 인원
    private int availableNOP;

    private String updatedate;


    private int views=0;

    @ElementCollection
    @CollectionTable(
            name = "festival_detail_styurls",
            joinColumns = @JoinColumn(name = "festival_detail_id") // ✅ 확실하게 FK 컬럼 생성
    )
    @Column(name="url")
    private List<String> styurls;

    @OneToMany(mappedBy = "festivalDetail",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<FestivalSchedule> schedules;
}
