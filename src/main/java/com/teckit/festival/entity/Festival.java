// package com.teckit.festival.entity;

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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fid", referencedColumnName = "id")
    private FestivalDetail festivalDetail;
    @Column(nullable = false)
    private String fname; // 공연명

    // 기간
    private LocalDate fdfrom;
    private LocalDate fdto;

    @Column(nullable = false)
    private String posterFile; // 썸네일

    @Column(nullable = false)
    private String fcltynm; // 장소명

    private String prfage;     // 관람 연령
    private String genrenm;  // 장르
    private String fstate;   // 상태

    public void linkDetail(FestivalDetail detail) {
        this.festivalDetail = detail;
        if (detail.getFestival() != null && detail.getFestival() != this) {
            detail.setFestival(this);
        } else if (detail.getFestival() == null) {
            detail.setFestival(this);
        }
    }
}
