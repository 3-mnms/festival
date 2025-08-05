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

    // 외부 ID (PF000001 형식)
    @Id
    @Column(unique = true)
    private String id;

    // 주최자 ID (nullable)
    @Column(nullable = true)
    private Long hid;

    // 공연명
    @Column(nullable = false)
    private String fname;

    // 공연 시작일
    @Column(nullable = false)
    private LocalDate fdfrom;

    // 공연 종료일
    @Column(nullable = false)
    private LocalDate fdto;

    // 포스터 URL
    @Column(nullable = false)
    private String poster;

    // 공연장 이름
    @Column(nullable = false)
    private String fcltynm;

    // 공연 지역
    @Column(nullable = false)
    private String area;

    // 장르명
    private String genrenm;

    // 공연 상태 (예: 공연중, 예정), 현재 날짜랑 비교해서 자동으로 바뀌게 ?
    private String fstate;

    // 공연 상세 정보 (1:1 연결)
    @OneToOne(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private FestivalDetail festivalDetail;
}
