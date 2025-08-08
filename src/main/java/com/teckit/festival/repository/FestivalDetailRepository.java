package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FestivalDetailRepository extends JpaRepository<FestivalDetail, String> {

    @Query("SELECT DISTINCT f.genrenm FROM FestivalDetail f WHERE f.genrenm IS NOT NULL")
    List<String> findDistinctGenrenm();

    @Query("SELECT f FROM FestivalDetail f WHERE f.id = :fid")
    Optional<FestivalDetail> findByFestivalId(@Param("fid") String fid);

    // 🎯 중복 fid 체크용
    boolean existsById(String fid);

}