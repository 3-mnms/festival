package com.teckit.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "festival_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FestivalReview extends BaseEntity{
    @Id
    @Column(name = "review_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(name = "review_content", nullable = false, length = 512)
    private String reviewContent;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fid")
    private FestivalDetail festivalDetail;
}
