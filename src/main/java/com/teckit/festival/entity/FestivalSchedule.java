package com.teckit.festival.entity;


import com.teckit.festival.enumeration.FestivalScheduleDay;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name="festival_schedule")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalSchedule {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fid")
    private FestivalDetail festivalDetail;

    @Enumerated(EnumType.STRING)
    private FestivalScheduleDay dayOfWeek;

    private String time;

}
