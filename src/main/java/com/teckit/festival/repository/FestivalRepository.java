package com.teckit.festival.repository;

import com.teckit.festival.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival,String> {
    boolean existsById(String id);

    List<Festival> findByGenrename(String genrename);

    List<Festival> findByFnameContaining(String keyword);

    List<Festival> findByGenrenameAndFnameContaining(String genrename,String keyword);
}
