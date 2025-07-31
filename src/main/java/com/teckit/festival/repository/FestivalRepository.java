package com.teckit.festival.repository;

import com.teckit.festival.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FestivalRepository extends JpaRepository<Festival,String> {
    boolean existsByFnameAndFdfromAndFdto(String fname, String fdfrom, String fdto);
}
