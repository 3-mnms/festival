package com.teckit.festival.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.teckit.festival.enumeration.FestivalScheduleDay;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "festival_schedule")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fid")
    @JsonBackReference
    private FestivalDetail festivalDetail;

    @Enumerated(EnumType.STRING)
    private FestivalScheduleDay dayOfWeek;

    private String time;

    public void setFestivalDetail(FestivalDetail detail) {
        this.festivalDetail = detail;
    }
}