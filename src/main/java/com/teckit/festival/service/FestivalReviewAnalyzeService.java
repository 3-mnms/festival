package com.teckit.festival.service;

import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalReviewAnalyze;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalReviewAnalyzeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FestivalReviewAnalyzeService {

    private final FestivalDetailRepository festivalDetailRepository;
    private final FestivalReviewAnalyzeRepository festivalReviewAnalyzeRepository;
    private final ChatClient chatClient;

    @Transactional
    public void analyzeReview(String newReview, String fId){
        FestivalDetail festivalDetail = festivalDetailRepository.findById(fId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        FestivalReviewAnalyze reviewAnalyze = festivalReviewAnalyzeRepository.findByFestivalDetail_Id(fId)
                .orElse(FestivalReviewAnalyze.builder().build());

        if (reviewAnalyze.getFestivalDetail() == null) {
            reviewAnalyze.setFestivalDetail(festivalDetail);
            festivalDetail.setFestivalReviewAnalyze(reviewAnalyze);
        }
        String summary = reviewAnalyze.getAnalyzeContent() != null ? reviewAnalyze.getAnalyzeContent() : "";

        int pCount = reviewAnalyze.getPositiveCount();
        int negCount = reviewAnalyze.getNegativeCount();
        int neuCount = reviewAnalyze.getNeutralCount();

        String prompt = buildPrompt(summary, newReview);
        String aiResult;

        try{
             aiResult = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        }catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_FAILED);
        }

        log.info("프롬프트 길이: {} chars", prompt.length());
        log.info("AI 응답 길이: {} chars", aiResult.length());

        String emotion = extractValue(aiResult, "감정");
        String newSummary = extractValue(aiResult, "요약");

        reviewAnalyze.setAnalyzeContent(newSummary);

        switch(emotion){
            case "긍정":
                pCount++;
                reviewAnalyze.setPositiveCount(pCount);
                break;
            case "부정":
                negCount++;
                reviewAnalyze.setNegativeCount(negCount);
                break;
            case "중립":
                neuCount++;
                reviewAnalyze.setNeutralCount(neuCount);
                break;
        }

        int total = pCount + negCount + neuCount;
        if (total > 0) {
            reviewAnalyze.setPositive(Math.round((pCount * 100.0) / total));
            reviewAnalyze.setNegative(Math.round((negCount * 100.0) / total));
            reviewAnalyze.setNeutral(Math.round((neuCount * 100.0) / total));
        }

        festivalReviewAnalyzeRepository.save(reviewAnalyze);

    }

    private String buildPrompt(String summary, String newReview) {
        return """
            당신은 공연 기대평을 요약하고 감정을 분석하는 AI입니다.
            기존 기대평 요약:
            "%s"
            
            새로운 기대평:
            "%s"

            위 내용을 바탕으로 다음을 해줘:
            1. 기존 기대평 요약에 새로운 기대평을 반영해 새로운 기대평 요약을 한 문장으로 작성해줘.
            2. 새 기대평의 감정을 '긍정', '부정', '중립' 중 하나로 판단해줘.

            응답은 반드시 아래 형식으로 해주세요 (다른 말 절대 하지 마세요):
            - 감정: 긍정
            - 요약: 기대평 내용을 반영한 한 문장 요약
            """.formatted(summary, newReview);
    }

    private String extractValue(String result, String key) {
        for (String line : result.split("\n")) {
            if (line.trim().startsWith("- " + key + ":")) {
                return line.split(":", 2)[1].trim();
            }
        }
        return "";
    }

}
