package com.teckit.festival.repository;

import com.teckit.festival.entity.Course;
import com.teckit.festival.entity.FestivalDetail;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByFestivalDetail(FestivalDetail festivalDetail);
}
