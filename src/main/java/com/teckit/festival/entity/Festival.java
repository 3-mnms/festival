package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Column(unique = true)
    private String id;

    @Column(nullable = true)
    private Long hid;

    @Column(nullable = false)
    private String fname;

    @Column(nullable = false)
    private LocalDate fdfrom;

    @Column(nullable = false)
    private LocalDate fdto;

    @Column(nullable = false)
    private String poster;

    @Column(nullable = false)
    private String fcltynm;

    @Column(nullable = false)
    private String area;

    private String genrenm;

    private String fstate;  // 공연 상태 (예정/공연중/완료) → Service에서 상태 변경 관리

    @OneToOne(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private FestivalDetail festivalDetail;
}