package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FestivalScheduleRepository extends JpaRepository<FestivalSchedule, Long> {

    // 주어진 공연 상세 ID(fid)에 해당하는 스케줄 목록을 요일 및 시간 순으로 정렬하여 조회
    @Query("""
        select s
        from FestivalSchedule s
        where s.festivalDetail.id = :fid
        order by s.dayOfWeek, s.time
    """)
    List<FestivalSchedule> findByFid(@Param("fid") String fid);

    // Summary: 주어진 공연 상세 ID(fid)에 해당하는 모든 스케줄을 삭제
    @Modifying
    @Transactional
    void deleteByFestivalDetail_Id(String fid);
}