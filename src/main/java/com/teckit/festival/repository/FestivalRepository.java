package com.teckit.festival.repository;

import com.teckit.festival.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, String> {

    List<Festival> findByGenrenm(String genre);

    List<Festival> findByFnameContaining(String keyword);

    List<Festival> findByGenrenmAndFnameContaining(String genre, String keyword);

    // 🎯 FestivalDetail의 fid로 Festival 찾기
    Optional<Festival> findByFestivalDetail_Id(String fid);

    // 🎯 주최자 ID로 등록한 공연 목록 조회
    List<Festival> findByLoginId(String loginId);
}