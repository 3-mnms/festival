package com.teckit.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Course {
    @Id
    @Column(name = "course_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @Column(name = "course1", nullable = false)
    private String course1;

    @Column(name = "course2", nullable = false)
    private String course2;

    @Column(name = "course3", nullable = false)
    private String course3;

    @Column(name = "course4", nullable = false)
    private String course4;

    @Column(name = "course5", nullable = false)
    private String course5;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_detail_id", nullable = false)
    private FestivalDetail festivalDetail;
}
