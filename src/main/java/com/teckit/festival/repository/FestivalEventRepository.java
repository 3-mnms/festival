package com.teckit.festival.repository;

import com.teckit.festival.entity.FestivalEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalEventRepository extends JpaRepository<FestivalEvent, Long> {
}