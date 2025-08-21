package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FestivalScheduleRepository extends JpaRepository<FestivalSchedule, Long> {

    @Query("""
        select s
        from FestivalSchedule s
        where s.festivalDetail.id = :fid
        order by s.dayOfWeek, s.time
    """)
    List<FestivalSchedule> findByFid(@Param("fid") String fid);

    List<FestivalSchedule> findByFestivalDetail_IdOrderByDayOfWeekAscTimeAsc(String fid);
}
