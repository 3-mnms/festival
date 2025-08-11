package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 내부 PK

    @OneToOne
    @JoinColumn(name = "fid", referencedColumnName = "id") // FK → FestivalDetail.id
    @JsonManagedReference
    private FestivalDetail festivalDetail;

    //@Column(nullable = false)
    //private String loginId; // 주최자 ID

    @Column(nullable = false)
    private String fname; // 공연명

    // 기간
    private LocalDate fdfrom;
    private LocalDate fdto;

    @Column(nullable = false)
    private String posterFile; // 썸네일

    @Column(nullable = false)
    private String fcltynm; // 장소명

    //@Column
    //private String area; // 지역

    private String fage;     // 연령 제한
    private String genrenm;  // 장르
    private String fstate;   // 상태

    //private int availableNOP; // 수용 인원

    // 편의 메서드
    public void setFid(String fid) {
        if (this.festivalDetail == null) {
            this.festivalDetail = new FestivalDetail();
        }
        this.festivalDetail.setId(fid);
    }
}
