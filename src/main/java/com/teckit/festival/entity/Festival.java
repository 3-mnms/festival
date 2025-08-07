package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Column(unique = true)
    private String id;

    @OneToOne(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private FestivalDetail festivalDetail;

    @Column(nullable = true)
    private Long hid;

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

    private String genrenm;

    private String fstate;  // 공연 상태 (예정/공연중/완료) → Service에서 상태 변경 관리

    private String faddress;

    private int ticketPick;

    private int maxPurchase;

    @ElementCollection
    private List<String> contentFile;
}