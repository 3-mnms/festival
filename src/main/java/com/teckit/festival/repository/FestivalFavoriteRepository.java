package com.teckit.festival.repository;

import com.teckit.festival.dto.request.MyFavoritesDTO;
import com.teckit.festival.entity.FestivalFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FestivalFavoriteRepository extends JpaRepository<FestivalFavorite, Long> {

    @Query("""
      select new com.teckit.festival.dto.request.MyFavoritesDTO(
        d.id, f.fname, f.posterFile
      )
      from FestivalFavorite fav
      join FestivalDetail d on d.id = fav.fid      
      join Festival f on f.festivalDetail = d      
      where fav.userId = :userId
      order by fav.createdAt desc
    """)
    Page<MyFavoritesDTO> findMyFavorites(Long userId, Pageable pageable);


    boolean existsByFidAndUserId(String fid, Long userId);
    Optional<FestivalFavorite> findByFidAndUserId(String fid, Long userId);
    long countByFid(String fid);

    Page<FestivalFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
