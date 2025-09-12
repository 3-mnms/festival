package com.teckit.festival.repository;

import com.teckit.festival.entity.NearbyFestival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NearbyFestivalRepository extends JpaRepository<NearbyFestival, Long> {
    List<NearbyFestival> findByUserIdOrderByDistanceAsc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from NearbyFestival n where n.userId = :userId")
    int deleteByUserId(Long userId);

}
