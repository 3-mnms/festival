package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FestivalReviewRepository extends JpaRepository<FestivalReview, Long> {
    Page<FestivalReview> findByFestivalDetail(FestivalDetail festivalDetail, Pageable pageable);
    Optional<FestivalReview> findByFestivalDetailAndUserId(FestivalDetail festivalDetail, Long userId);
}
