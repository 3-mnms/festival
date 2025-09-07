package com.teckit.festival.repository;

import com.teckit.festival.dto.NearbyFestivalInterface;
import com.teckit.festival.entity.FestivalDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FestivalDetailRepository extends JpaRepository<FestivalDetail, String> {

    // 모든 공연 상세 정보에서 중복되지 않는 장르 목록을 조회
    @Query("SELECT DISTINCT f.genrenm FROM FestivalDetail f WHERE f.genrenm IS NOT NULL")
    List<String> findDistinctGenrenm();

    // 주어진 공연 ID(fid)로 공연 상세 정보를 조회
    @Query("SELECT f FROM FestivalDetail f WHERE f.id = :fid")
    Optional<FestivalDetail> findByFestivalId(@Param("fid") String fid);

    // 주어진 ID로 공연 상세 정보와 연관된 스케줄을 함께 로드
    @Query("SELECT fd FROM FestivalDetail fd LEFT JOIN FETCH fd.schedules WHERE fd.id = :id")
    Optional<FestivalDetail> findByIdWithSchedules(@Param("id") String id);

    // 주어진 ID로 공연 상세 정보와 함께 contentFile을 로드
    @EntityGraph(attributePaths = {"contentFile"})
    @Query("select d from FestivalDetail d where d.id = :fid")
    Optional<FestivalDetail> findGraphByFid(@Param("fid") String fid);

    // 주어진 ID를 가진 공연 상세 정보가 존재하는지 확인
    boolean existsById(String fid);

    @Query("select fd from FestivalDetail fd where fd.isGeocoded= 'PENDING'")
    List<FestivalDetail> findGeocoding(Pageable pageable);

    @Query(value = """
    SELECT fd.id AS festivalDetailId, fd.fname AS name, fd.fcast AS cast, fd.faddress AS address, fd.latitude AS latitude, fd.longitude AS longitude, DATE(fd.fdto) AS finishDate,
        (6371 * 2 * ASIN(SQRT(POW(SIN(RADIANS((:lat - fd.latitude)/2)), 2) +
        COS(RADIANS(:lat)) * COS(RADIANS(fd.latitude)) *
        POW(SIN(RADIANS((:lon - fd.longitude)/2)), 2)))) AS distance
    FROM festival_detail fd
    WHERE DATE(fd.fdto) >= CURRENT_DATE
      AND fd.latitude IS NOT NULL AND fd.longitude IS NOT NULL
      AND fd.latitude BETWEEN (:lat - (:radiusKm / 111.32)) AND (:lat + (:radiusKm / 111.32))
      AND fd.longitude BETWEEN (:lon - (:radiusKm / (111.32 * COS(RADIANS(:lat))))) AND (:lon + (:radiusKm / (111.32 * COS(RADIANS(:lat)))))
    HAVING distance <= :radiusKm
    ORDER BY distance ASC
    LIMIT 3
    """, nativeQuery = true)
    List<NearbyFestivalInterface> findTop3NearByFestivalDetail(double lat, double lon, double radiusKm);


}