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
    @Column(name = "course_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @Column(name = "course1", nullable = false)
    private String course1;

    @Column(name = "course2", nullable = false)
    private String course2;

    @Column(name = "course3", nullable = false)
    private String course3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nearby_id", nullable = false)
    private NearbyFestival nearbyFestival;
}
