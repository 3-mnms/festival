package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 자동 생성 PK

    @OneToOne
    @JoinColumn(name = "fid", referencedColumnName = "id") // FK → FestivalDetail.id
    @JsonManagedReference
    private FestivalDetail festivalDetail;

    @Column(nullable = true)
    private String loginId;

    @Column(nullable = false)
    private String fname;

    @Column(nullable = false)
    private LocalDate fdfrom;

    @Column(nullable = false)
    private LocalDate fdto;

    @Column(nullable = false)
    private String posterFile;

    @Column(nullable = false)
    private String fcltynm;

    @Column(nullable = false)
    private String area;

    private String fage;
    private String genrenm;
    private String fstate;
    private String faddress;

    private int ticketPick;       // 티컷 수령 방법 (0~2)
    private int maxPurchase;      //
    private int ticketPrice;      //
    private int availableNOP;     //

    @ElementCollection
    private List<String> contentFile;

    // ✨ fid 설정 하는 메서드 수정
    public void setFid(String fid) {
        if (this.festivalDetail == null) {
            this.festivalDetail = new FestivalDetail();
        }
        this.festivalDetail.setId(fid);
    }
}