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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "festival_id", referencedColumnName = "id")
    @JoinColumn(name = "fid")
    @JsonManagedReference
    private Festival festival;

    private String fcltyid;
    private String fname;
    private String fdfrom;
    private String fdto;
    private String fcltynm;
    private String fcast;
    private String fage;

    private String faddress;

    @Column(nullable = false)
    private int ticketPrice;

    private int maxPurchase;

    private int ticketPick;

    @Column(length = 1000)
    private String posterFile;

    @Lob
    private String story;

    private String genrenm;
    private String fstate;
    private String visit;

    private int availableNOP;

    private String updatedate;

    @Builder.Default
    private int views = 0;

    // 상세 이미지 URL 리스트 (상세 페이지 이미지들)
    @ElementCollection
    @CollectionTable(
            name = "festival_detail_contents",  // 테이블명
            joinColumns = @JoinColumn(name = "festival_detail_id")  // FK
    )
    @Column(name = "content_image") // 컬럼명
    private List<String> contentFile;

    // 공연 일정 정보 (요일 + 시간)
    @OneToMany(mappedBy = "festivalDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<FestivalSchedule> schedules;
}