package com.teckit.festival.scheduler;

import com.teckit.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FestivalScheduler {
    private final FestivalService festivalService;

    /**
     * 매주 일요일 새벽 3시에 실행되어 당일부터 3개월 후까지의 공연 데이터를 수집
     * `0 0 3 ? * SUN` : 초(0) 분(0) 시(3) 일(상관없음) 월(모든 달) 요일(일요일)
     */
    @Scheduled(cron = "0 55 10 ? * *")
    //@Scheduled(cron = "0 0 4 ? * SUN")
    public void fetchAndSaveWeeklyFestivals() {
        log.info("주간 API 수집 스케줄러 실행: {}", LocalDate.now());

        LocalDate today = LocalDate.now();
        LocalDate threeMonthsLater = today.plusMonths(3); //test용 (나중에 3으로 변경)

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String stdate = today.format(formatter);
        String eddate = threeMonthsLater.format(formatter);

        try {
            log.info("스케줄러: {}~{} 기간 ID 수집 시작", stdate, eddate);
            List<String> ids = festivalService.fetchIdsByPeriod(stdate, eddate);
            log.info("스케줄러: ID {}개 수집 완료", ids.size());

            // 수집된 ID 목록으로 상세 데이터 저장
            festivalService.fetchAndSaveFestivalDetails(ids);
            log.info("스케줄러: ID {}개 상세 정보 저장 완료", ids.size());

        } catch (Exception e) {
            log.error("API 스케줄링 작업 실패", e);
        }
    }
}