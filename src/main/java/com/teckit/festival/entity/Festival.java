package com.teckit.festival.entity;

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
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 내부 PK

    @OneToOne
    @JoinColumn(name = "fid", referencedColumnName = "id") // FK → FestivalDetail.id
    @JsonManagedReference
    private FestivalDetail festivalDetail;

    @Column
    private String loginId;

    @Column(nullable = false)
    private String fname;

    // 상세에서는 문자열이므로 이 필드는 nullable로 유지(원하면 파싱해서 set)
    private String fdfrom;
    private String fdto;

    @Column(nullable = false)
    private String posterFile;

    @Column(nullable = false)
    private String fcltynm;

    @Column(nullable = true)
    private String area;

    private String fage;
    private String genrenm;
    private String fstate;
    private String faddress;

    private int ticketPick;
    private int maxPurchase;
    private int ticketPrice;
    private int availableNOP;

    @ElementCollection
    private List<String> contentFile;

    // 편의 메서드(필요하면)
    public void setFid(String fid) {
        if (this.festivalDetail == null) {
            this.festivalDetail = new FestivalDetail();
        }
        this.festivalDetail.setId(fid);
    }
}
