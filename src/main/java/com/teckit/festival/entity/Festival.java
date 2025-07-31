package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Festival {
    @Id
    @Column(unique = true)
    private String id;

    @Column(nullable = false)
    private String fname;

    @Column(nullable = false)
    private String fdfrom;

    @Column(nullable = false)
    private String fdto;

    @Column(nullable = false)
    private String poster;

    @Column(nullable = false)
    private String fcltynm;

    @Column(nullable = false)
    private String area;

    private String genrename;

    private String openrun;

//    현재 날짜랑 비교해서 자동으로 바뀌게 ?
//    schedule 설정
    private String fstate;

    @OneToOne(mappedBy = "festival",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonBackReference
    private FestivalDetail festivalDetail;
}
