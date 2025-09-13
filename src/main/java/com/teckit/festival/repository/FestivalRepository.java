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

    // 장르로 공연 목록을 조회
    List<Festival> findByGenrenm(String genre);

    // 공연 이름에 키워드가 포함된 공연 목록을 조회
    List<Festival> findByFnameContaining(String keyword);

    // 장르와 키워드로 공연 목록을 조회
    List<Festival> findByGenrenmAndFnameContaining(String genre, String keyword);

    // 특정 사용자가 등록한 공연 목록을 조회
    List<Festival> findByFestivalDetail_UserId(Long userId);

    // 모든 공연 목록을 DTO 형태로 페이징하여 조회
    @Query(
            value = """
    select new com.teckit.festival.dto.response.FestivalListResponseDTO(
      f.festivalDetail.id, f.fname, f.fdfrom, f.fdto, f.posterFile, f.fcltynm, f.genrenm
    )
    from Festival f JOIN f.festivalDetail fd
    ORDER BY fd.views DESC
  """,
            countQuery = "select count(f) from Festival f"
    )
    Page<FestivalListResponseDTO> findList(Pageable pageable);

    // 공연 상세 ID(fid)를 통해 Festival 엔티티를 조회
    Optional<Festival> findByFestivalDetail_Id(String fid);

    // 공연 상세 ID(fid)를 가진 Festival 엔티티가 존재하는지 확인
    boolean existsByFestivalDetail_Id(String fid);

    // 공연완료 제외 + 포스터/스토리 있는 공연만 조회 (조회수 순 정렬) + 스케줄 존재 필터링
    @Query("SELECT DISTINCT f FROM Festival f " +
            "JOIN f.festivalDetail fd " +
            "JOIN f.festivalDetail.schedules s " +  // 스케줄이 있는 경우만 매칭됨
            "WHERE f.fstate <> '공연완료' " +
            "AND (fd.story IS NOT NULL OR fd.contentFile IS NOT EMPTY) " +
            "ORDER BY fd.views DESC")
    Page<Festival> findLiveFestivalsWithContentAndStory(Pageable pageable);

    // 장르별 + 공연완료 제외 + 스토리/포스터 있는 공연만 조회 + 스케줄 존재 필터링
    @Query("SELECT DISTINCT f FROM Festival f " +
            "JOIN f.festivalDetail fd " +
            "JOIN f.festivalDetail.schedules s " +  // 스케줄 필터
            "WHERE f.genrenm = :genrenm " +
            "AND f.fstate <> '공연완료' " +
            "AND (fd.story IS NOT NULL OR fd.contentFile IS NOT EMPTY) " +
            "ORDER BY fd.views DESC")
    Page<Festival> findLiveFestivalsByGenrenm(String genrenm, Pageable pageable);
}