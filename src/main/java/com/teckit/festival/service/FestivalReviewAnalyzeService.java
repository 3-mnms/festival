package com.teckit.festival.service;

import com.teckit.festival.dto.request.AiReviewRequestDTO;
import com.teckit.festival.dto.response.AiReviewResponseDTO;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalReviewAnalyze;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalReviewAnalyzeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalReviewAnalyzeService {

    private final FestivalDetailRepository festivalDetailRepository;
    private final FestivalReviewAnalyzeRepository festivalReviewAnalyzeRepository;
    private final WebClient webClient;

    @Transactional
    public void analyzeReview(String newReview, String fId){
        FestivalDetail festivalDetail = festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        FestivalReviewAnalyze reviewAnalyze = festivalReviewAnalyzeRepository.findByFestivalDetail(festivalDetail)
                .orElse(FestivalReviewAnalyze.builder()
                        .festivalDetail(festivalDetail)
                        .build());

        AiReviewResponseDTO responseDTO = callAiReviewAnalyze(reviewAnalyze, newReview);
        reviewAnalyze.updateReviewAnalyze(responseDTO);

        festivalReviewAnalyzeRepository.save(reviewAnalyze);
    }

    private AiReviewResponseDTO callAiReviewAnalyze(FestivalReviewAnalyze reviewAnalyze, String newReview) {

        String summary = reviewAnalyze.getAnalyzeContent() != null ? reviewAnalyze.getAnalyzeContent() : "";

        AiReviewRequestDTO reviewRequest = AiReviewRequestDTO.from(
                summary, newReview, reviewAnalyze.getPositiveCount(), reviewAnalyze.getNegativeCount(), reviewAnalyze.getNeutralCount()
        );

        AiReviewResponseDTO response = webClient.post()
                .uri("/review/analyze")
                .bodyValue(reviewRequest)
                .retrieve()
                .bodyToMono(AiReviewResponseDTO.class)
                .block();

        if (response == null) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_FAILED);
        }

        return response;
    }


}
