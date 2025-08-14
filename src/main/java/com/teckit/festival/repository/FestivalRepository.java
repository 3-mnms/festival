package com.teckit.festival.repository;

import com.teckit.festival.dto.response.FestivalListResponseDTO;
import com.teckit.festival.entity.Festival;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    List<Festival> findByGenrenm(String genre);
    List<Festival> findByFnameContaining(String keyword);
    List<Festival> findByGenrenmAndFnameContaining(String genre, String keyword);

    List<Festival> findByFestivalDetail_UserId(Long userId);

    @Query(
            value = """
    select new com.teckit.festival.dto.response.FestivalListResponseDTO(
      f.festivalDetail.id, f.fname, f.fdfrom, f.fdto, f.posterFile, f.fcltynm
    )
    from Festival f
  """,
            countQuery = "select count(f) from Festival f"
    )
    Page<FestivalListResponseDTO> findList(Pageable pageable);

    Optional<Festival> findByFestivalDetail_Id(String fid);
    boolean existsByFestivalDetail_Id(String fid);

    // Optional<Festival> findByFid(String fid);                 // ** 삭제: Festival에 fid 필드 없음 **
    // 필요하다면 아래처럼 JPQL로 대체 가능(하지만 위 메서드가 있어 굳이 필요 없음)
    // @Query("select f from Festival f where f.festivalDetail.id = :fid")
    // Optional<Festival> findByFid(@Param("fid") String fid);   // **대안**
}
