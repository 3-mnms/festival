package com.teckit.festival.repository;

import com.teckit.festival.entity.Activity;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.enumeration.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByFestivalDetailAndActivityType(FestivalDetail festivalDetail, ActivityType activityType);
}