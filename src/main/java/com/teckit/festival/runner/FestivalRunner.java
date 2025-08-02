package com.teckit.festival.runner;

import com.teckit.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FestivalRunner implements CommandLineRunner {

    private final FestivalService festivalService;

    @Override
    public void run(String... args) throws Exception {

        int nowMonth = LocalDate.now().getMonthValue(); // 1~12
        String stdate = String.format("2025%02d01", nowMonth);
        String eddate = String.format("2025%02d31", nowMonth);

        for(int i=nowMonth;i<=nowMonth+1;i++){
            festivalService.fetchAndSaveFestivalListAndDetail(stdate,eddate);
        }
    }
}