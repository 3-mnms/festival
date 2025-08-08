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
        int year = LocalDate.now().getYear();

        try {
            for (int i = 0; i < 2; i++) { // 이번 달 + 다음 달
                int targetMonth = nowMonth + i;
                int targetYear = year;

                if (targetMonth > 12) { // 12월 넘어가면 연도 +1
                    targetMonth -= 12;
                    targetYear += 1;
                }

                LocalDate startDate = LocalDate.of(targetYear, targetMonth, 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth()); // 말일 계산

                String stdate = startDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
                String eddate = endDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

                festivalService.fetchAndSaveFestivalListAndDetail(stdate, eddate);
            }
        } catch (Exception e) {
            // 앱은 뜨게 하고, 수집 실패만 로그로 남김
            //log.error("Startup data sync failed", e);
        }
    }
}
