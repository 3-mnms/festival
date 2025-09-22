package com.teckit.festival.entity;

import com.teckit.festival.enumeration.ActivityType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Activity {
    @Id
    @Column(name = "activity_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "address_name", nullable = false)
    private String addressName;

    @Column(name = "latitude",  columnDefinition = "DECIMAL(10,7)")
    private Double latitude; //위도

    @Column(name = "longitude",  columnDefinition = "DECIMAL(10,7)")
    private Double longitude;//경도

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ActivityType activityType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "festival_detail_id", nullable = false)
    private FestivalDetail festivalDetail;
}
