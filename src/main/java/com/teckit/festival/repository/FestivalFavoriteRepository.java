package com.teckit.festival.repository;

import com.teckit.festival.dto.request.MyFavoritesDTO;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface FestivalFavoriteRepository extends JpaRepository<FestivalFavorite, Long> {

    // 주어진 사용자 ID에 해당하는 관심 상품 목록을 DTO 형태로 페이징하여 조회
    @Query("""
      select new com.teckit.festival.dto.request.MyFavoritesDTO(
        d.id, f.fname, f.posterFile
      )
      from FestivalFavorite fav
      join fav.festivalDetail d
      join d.festival f
      where fav.userId = :userId
      order by fav.createdAt desc
    """)
    Page<MyFavoritesDTO> findMyFavorites(@Param("userId") Long userId, Pageable pageable);

    // 특정 공연과 사용자에 대해 관심 상품이 존재하는지 확인
    boolean existsByFestivalDetailAndUserId(FestivalDetail festivalDetail, Long userId);

    // 특정 공연과 사용자에 해당하는 관심 상품을 조회
    Optional<FestivalFavorite> findByFestivalDetailAndUserId(FestivalDetail festivalDetail, Long userId);

    // 특정 공연의 관심 상품 수를 계산
    long countByFestivalDetail(FestivalDetail festivalDetail);

    // 특정 공연과 사용자에 해당하는 관심 상품을 삭제
    @Modifying
    @Transactional
    long deleteByFestivalDetailAndUserId(FestivalDetail festivalDetail, Long userId);
}