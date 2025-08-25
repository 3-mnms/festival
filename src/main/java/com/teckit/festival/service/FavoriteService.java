package com.teckit.festival.service;

import com.teckit.festival.dto.request.MyFavoritesDTO;
import com.teckit.festival.dto.response.FavoriteToggleResponse;
import com.teckit.festival.dto.response.MyFavoritesListResponse;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalFavorite;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FestivalFavoriteRepository favoriteRepo;
    private final FestivalDetailRepository festivalDetailRepository;

    // Helper method to find FestivalDetail
    private FestivalDetail getFestivalDetail(String fid) {
        return festivalDetailRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    // 관심 상품 등록
    @Transactional
    public FavoriteToggleResponse createFavorites(String fid, Long userId) {
        FestivalDetail festivalDetail = getFestivalDetail(fid);
        try {
            // 관심 상품이 이미 존재하면 DataIntegrityViolationException 발생
            favoriteRepo.save(FestivalFavorite.builder().festivalDetail(festivalDetail).userId(userId).build());
        } catch (DataIntegrityViolationException e) {
            // 중복된 경우, BusinessException을 던져서 전역 예외 핸들러로 보낸다.
            throw new BusinessException(ErrorCode.DUPLICATE_FAVORITE, "이미 관심 상품에 등록된 공연입니다.");
        }
        long cnt = favoriteRepo.countByFestivalDetail(festivalDetail);
        return new FavoriteToggleResponse(true, cnt);
    }

    // 관심 상품 해제
    @Transactional
    public FavoriteToggleResponse deleteFavorites(String fid, Long userId) {
        FestivalDetail festivalDetail = getFestivalDetail(fid);
        long deletedCount = favoriteRepo.deleteByFestivalDetailAndUserId(festivalDetail, userId);
        if (deletedCount == 0) {
            // 삭제할 대상이 없는 경우
            throw new BusinessException(ErrorCode.FAVORITE_NOT_FOUND, "삭제할 관심 상품을 찾을 수 없습니다.");
        }
        long cnt = favoriteRepo.countByFestivalDetail(festivalDetail);
        return new FavoriteToggleResponse(false, cnt);
    }

    // 관심 상품 등록 여부 조회
    public boolean readFavorites(String fid, Long userId) {
        FestivalDetail festivalDetail = getFestivalDetail(fid);
        return favoriteRepo.existsByFestivalDetailAndUserId(festivalDetail, userId);
    }

    // 관심 상품 등록 수 조회
    public long readCountFavorites(String fid) {
        FestivalDetail festivalDetail = getFestivalDetail(fid);
        return favoriteRepo.countByFestivalDetail(festivalDetail);
    }

    // 사용자 별 관심 등록 리스트 조회
    public MyFavoritesListResponse readMyFavorites(Long userId, Pageable pageable) {
        Page<MyFavoritesDTO> page = favoriteRepo.findMyFavorites(userId, pageable);
        return new MyFavoritesListResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}