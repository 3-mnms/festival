package com.teckit.festival.repository;

import com.teckit.festival.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
    List<Festival> findByGenrenm(String genre);
    List<Festival> findByFnameContaining(String keyword);
    List<Festival> findByGenrenmAndFnameContaining(String genre, String keyword);
    Optional<Festival> findByFestivalDetail_Id(String fid);
    List<Festival> findByFestivalDetail_UserId(Long userId);
    boolean existsByFestivalDetail_Id(String id);
}

