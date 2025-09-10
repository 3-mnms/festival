package com.teckit.festival.repository;

import com.teckit.festival.entity.NearbyFestival;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NearbyFestivalRepository extends JpaRepository<NearbyFestival, Long> {
    List<NearbyFestival> findByUserIdOrderByDistanceAsc(Long userId);

    Long deleteByUserId(Long userId);


}
