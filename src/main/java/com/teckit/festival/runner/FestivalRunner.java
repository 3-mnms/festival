package com.teckit.festival.runner;

import com.teckit.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FestivalRunner implements CommandLineRunner {

    private final FestivalService festivalService;

    @Override
    public void run(String... args) throws Exception {
        festivalService.fetchAndSaveFestivalListAndDetail();
    }
}