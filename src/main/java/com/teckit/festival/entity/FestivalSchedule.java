package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.teckit.festival.enumeration.FestivalScheduleDay;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "festival_schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK → FestivalDetail.id (문자열)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fid", nullable = false)
    @JsonBackReference
    private FestivalDetail festivalDetail;

    @Enumerated(EnumType.STRING)
    private FestivalScheduleDay dayOfWeek;

    private String time;
}
