package com.teckit.festival.service;

import com.teckit.festival.dto.request.FestivalReviewRequestDTO;
import com.teckit.festival.dto.response.FestivalReviewResponseDTO;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalReview;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalReviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalReviewService {
    private final FestivalDetailRepository festivalDetailRepository;
    private final FestivalReviewRepository festivalReviewRepository;

    public List<FestivalReviewResponseDTO> getReviews(String fId) {
        FestivalDetail festivalDetail = festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        List<FestivalReview> festivalReviewList = festivalReviewRepository.findByFestivalDetail(festivalDetail);

        return festivalReviewList.stream()
                .map(FestivalReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public FestivalReviewResponseDTO createReview(Long userId, @Valid FestivalReviewRequestDTO festivalReviewRequestDTO, String fId) {
        FestivalDetail festivalDetail = festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        FestivalReview festivalReview = festivalReviewRequestDTO.toEntity(userId, festivalReviewRequestDTO, festivalDetail);
        festivalReviewRepository.save(festivalReview);

        return FestivalReviewResponseDTO.fromEntity(festivalReview);
    }

    @Transactional
    public FestivalReviewResponseDTO updateReview(Long userId, @Valid FestivalReviewRequestDTO festivalReviewRequestDTO, String fId, Long rId) {
        festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        FestivalReview festivalReview = festivalReviewRepository.findById(rId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if(!festivalReview.getUserId().equals(userId))
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);

        festivalReview.setReviewContent(festivalReviewRequestDTO.getReviewContent());
        festivalReviewRepository.save(festivalReview);

        return FestivalReviewResponseDTO.fromEntity(festivalReview);
    }

    @Transactional
    public void deleteReview(Long userId, String fId, Long rId) {
        festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        FestivalReview festivalReview = festivalReviewRepository.findById(rId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if(!festivalReview.getUserId().equals(userId))
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);

        festivalReviewRepository.delete(festivalReview);
    }
}
