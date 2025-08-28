package com.teckit.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "festival_review_analyzes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class FestivalReviewAnalyze {
    @Id
    @Column(name = "analyze_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analyzeId;

    @Column(name = "analyze_content", length = 512)
    private String analyzeContent;

    @Column(name = "positive")
    @Builder.Default
    private double positive = 0;

    @Column(name = "negative")
    @Builder.Default
    private double negative = 0;

    @Column(name = "neutral")
    @Builder.Default
    private double neutral = 0;

    @Column(name = "positiveCount")
    @Builder.Default
    private int positiveCount = 0;

    @Column(name = "negativeCount")
    @Builder.Default
    private int negativeCount = 0;

    @Column(name = "neutralCount")
    @Builder.Default
    private int neutralCount = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fid")
    private FestivalDetail festivalDetail;

}
