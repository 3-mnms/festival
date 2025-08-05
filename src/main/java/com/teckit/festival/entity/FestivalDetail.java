package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;
import lombok.*;

import java.util.List;

@DynamicInsert
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
    @JsonBackReference
    private Festival festival;

    private String fcltyid;
    private String fname;
    private String fdfrom;
    private String fdto;
    private String fcltynm;
    private String fcast;
    private String fage;
    private int ticketPrice;

    @Column(length = 1000)
    private String poster;

    @Lob
    private String story;

    private String genrenm;
    private String fstate;
    private String visit;
    @Column(name = "available_nop")
    private Integer availableNop = 0;
    private String updatedate;

    // 조회수 (기본값 0)
    @Builder.Default
    private int views = 0;

    @ElementCollection
    @CollectionTable(name = "festival_detail_styurls", joinColumns = @JoinColumn(name = "festival_detail_id"))
    @Column(name = "url")
    private List<String> styurls;

    @OneToMany(mappedBy = "festivalDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FestivalSchedule> schedules;
}
