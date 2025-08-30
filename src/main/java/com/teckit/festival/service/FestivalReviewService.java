package com.teckit.festival.service;

import com.teckit.festival.dto.request.FestivalReviewRequestDTO;
import com.teckit.festival.dto.response.FestivalReviewResponseDTO;
import com.teckit.festival.dto.response.FestivalReviewResultDTO;
import com.teckit.festival.dto.response.ReviewAnalyzeResponseDTO;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalReview;
import com.teckit.festival.entity.FestivalReviewAnalyze;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalReviewAnalyzeRepository;
import com.teckit.festival.repository.FestivalReviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalReviewService {
    private final FestivalDetailRepository festivalDetailRepository;
    private final FestivalReviewRepository festivalReviewRepository;
    private final FestivalReviewAnalyzeRepository festivalReviewAnalyzeRepository;
    private final FestivalReviewAnalyzeService analyzeService;

    public FestivalReviewResultDTO getReviews(String fId, Pageable pageable) {
        FestivalDetail festivalDetail = festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        Page<FestivalReview> festivalReviewList = festivalReviewRepository.findByFestivalDetail(festivalDetail, pageable);
        FestivalReviewAnalyze festivalReviewAnalyze = festivalReviewAnalyzeRepository.findByFestivalDetail(festivalDetail)
                .orElse(null);

        Page<FestivalReviewResponseDTO> festivalReviewListDTO = festivalReviewList.map(FestivalReviewResponseDTO::fromEntity);

        FestivalReviewResultDTO reviewResultDTO = FestivalReviewResultDTO.builder()
                .reviews(festivalReviewListDTO)
                .analyze(festivalReviewAnalyze != null ? ReviewAnalyzeResponseDTO.fromEntity(festivalReviewAnalyze) : null)
                .build();
        return reviewResultDTO;
    }

    public FestivalReviewResponseDTO getMyReview(String fId, Long userId) {
        FestivalDetail festivalDetail = festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        FestivalReviewResponseDTO reviewResponseDTO = festivalReviewRepository.findByFestivalDetailAndUserId(festivalDetail, userId)
                .map(FestivalReviewResponseDTO::fromEntity)
                .orElse(null);

        return reviewResponseDTO;
    }

    @Transactional
    public FestivalReviewResponseDTO createReview(Long userId, @Valid FestivalReviewRequestDTO festivalReviewRequestDTO, String fId) {
        FestivalDetail festivalDetail = festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        festivalReviewRepository.findByFestivalDetailAndUserId(festivalDetail, userId)
                .ifPresent(review ->{
                    throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
                });

        FestivalReview festivalReview = festivalReviewRequestDTO.toEntity(userId);
        festivalDetail.getFestivalReviews().add(festivalReview);
        festivalReview.setFestivalDetail(festivalDetail);

//        analyzeService.analyzeReview(festivalReviewRequestDTO.getReviewContent(), fId);

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
    public void deleteReview(Long userId, String role, String fId, Long rId) {
        festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        FestivalReview festivalReview = festivalReviewRepository.findById(rId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if(!role.equals("ADMIN")) {
            if (!festivalReview.getUserId().equals(userId))
                throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);
        }
        festivalReviewRepository.delete(festivalReview);
    }
}
