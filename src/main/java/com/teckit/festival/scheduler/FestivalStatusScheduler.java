package com.teckit.festival.scheduler;

import com.teckit.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FestivalStatusScheduler {

    private final FestivalService festivalService;

    // 매일 자정에 실행되도록 설정
    @Scheduled(cron = "0 0 0 * * *")
    // test
    //@Scheduled(cron = "0 59 13 ? * TUE")
    public void updateFestivalStatus() {
        festivalService.updateAllFestivalStatus();
    }
}