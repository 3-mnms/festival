package com.teckit.festival.repository;

import com.teckit.festival.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalRepository extends JpaRepository<Festival, String> {

    List<Festival> findByGenrenm(String genre);

    List<Festival> findByFnameContaining(String keyword);

    List<Festival> findByGenrenmAndFnameContaining(String genre, String keyword);

    List<Festival> findByHid(Long hid);

}