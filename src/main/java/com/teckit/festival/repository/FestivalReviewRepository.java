package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalReviewRepository extends JpaRepository<FestivalReview, Long> {
    List<FestivalReview> findByFestivalDetail(FestivalDetail festivalDetail);

}
