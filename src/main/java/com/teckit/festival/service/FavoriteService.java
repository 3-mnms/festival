package com.teckit.festival.service;

import com.teckit.festival.dto.request.MyFavoritesDTO;
import com.teckit.festival.dto.response.FavoriteToggleResponse;
import com.teckit.festival.dto.response.MyFavoritesListResponse;
import com.teckit.festival.entity.FestivalFavorite;
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

    // 관심 상품 등록
    @Transactional
    public FavoriteToggleResponse createFavorites(String fid, Long userId) {
        if (!favoriteRepo.existsByFidAndUserId(fid, userId)) {
            try {
                favoriteRepo.save(FestivalFavorite.builder().fid(fid).userId(userId).build());
            } catch (DataIntegrityViolationException ignore) {

            }
        }
        long cnt = favoriteRepo.countByFid(fid);
        return new FavoriteToggleResponse(true, cnt);
    }

    // 관심 상품 해제
    @Transactional
    public FavoriteToggleResponse deleteFavorites(String fid, Long userId) {
        favoriteRepo.findByFidAndUserId(fid, userId).ifPresent(favoriteRepo::delete);
        long cnt = favoriteRepo.countByFid(fid);
        return new FavoriteToggleResponse(false, cnt);
    }

    // 관심 상품 등록 여부 조회
    public boolean readFavorites(String fid, Long userId) {
        if (userId == null) return false; // 비로그인
        return favoriteRepo.existsByFidAndUserId(fid, userId);
    }

    // 관심 상품 등록 수 조회
    public long readCountFavorites(String fid) {
        return favoriteRepo.countByFid(fid);
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
