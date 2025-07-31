package com.teckit.festival.scheduler;

import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FestivalScheduler {
    private final FestivalService festivalService;

//    @Scheduled(cron = "0 0 4 * * *")
//    public void fetchAndSaveDailyFestival(){
//    }
}
