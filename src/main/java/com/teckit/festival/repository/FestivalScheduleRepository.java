package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalScheduleRepository extends JpaRepository<FestivalSchedule,Long> {
    @Override
    <S extends FestivalSchedule> List<S> saveAll(Iterable<S> entities);
}
