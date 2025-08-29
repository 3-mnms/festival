package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalReview;
import com.teckit.festival.entity.FestivalReviewAnalyze;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FestivalReviewAnalyzeRepository extends JpaRepository<FestivalReviewAnalyze, Long> {
    Optional<FestivalReviewAnalyze> findByFestivalDetail(FestivalDetail festivalDetail);
}
