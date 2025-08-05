package com.teckit.festival.repository;

import com.teckit.festival.entity.Festival;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, String> {

    boolean existsById(String id);

    List<Festival> findByHid(Long hid);

    List<Festival> findByGenrenm(String genrenm);

    List<Festival> findByFnameContaining(String keyword);

    List<Festival> findByGenrenmAndFnameContaining(String genrenm, String keyword);

    @Query("SELECT f FROM Festival f WHERE f.id LIKE 'PF%' ORDER BY f.id DESC")
    List<Festival> findLastFestivalId(Pageable pageable);
}
