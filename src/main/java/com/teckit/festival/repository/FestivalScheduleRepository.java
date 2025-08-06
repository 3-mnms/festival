package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FestivalScheduleRepository extends JpaRepository<FestivalSchedule, Long> {

    @Query("SELECT s FROM FestivalSchedule s WHERE s.festivalDetail.id = :festivalDetailId")
    List<FestivalSchedule> findByFestivalDetailId(@Param("festivalDetailId") Long festivalDetailId);
}
