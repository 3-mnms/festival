package com.teckit.festival.scheduler;

import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FestivalScheduler {
    private final FestivalService festivalService;

    @Scheduled(cron = "0 0 4 * * *")
    public void fetchAndSaveDailyFestival(){
        int nowMonth = LocalDate.now().getMonthValue(); // 1~12
        String stdate = String.format("2025%02d01", nowMonth);
        String eddate = String.format("2025%02d31", nowMonth);

        for(int i=nowMonth;i<=nowMonth+1;i++){
            festivalService.fetchAndSaveFestivalListAndDetail(stdate,eddate);
        }
    }
}
