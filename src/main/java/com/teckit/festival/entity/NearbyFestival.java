package com.teckit.festival.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "nearby_festivals",
        uniqueConstraints = {
            @UniqueConstraint(name = "uq_nearby_user_fid", columnNames = {"user_id", "fid"})
        },
        indexes = {
                @Index(name = "idx_nearby_user", columnList = "user_id"),
                @Index(name = "idx_nearby_fid", columnList = "fid"),
                @Index(name = "idx_nearby_user_updated", columnList = "user_id, updated_at")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class NearbyFestival extends BaseEntity {
    @Id
    @Column(name = "nearby_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nearbyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "latitude",  columnDefinition = "DECIMAL(10,7)")
    private Double latitude; //위도

    @Column(name = "longitude",  columnDefinition = "DECIMAL(10,7)")
    private Double longitude;//경도

    @Column(name = "finish_date")
    private LocalDate finishDate;

    @Column(name = "distance")
    private Double distance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fid", nullable = false)
    private FestivalDetail festivalDetail;
}