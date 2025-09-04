package com.teckit.festival.scheduler;

import com.teckit.festival.service.FestivalGeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FestivalGeocodeScheduler {
    private final FestivalGeocodeService festivalGeocodeService;

    @Scheduled(
            fixedDelayString = "${festival.geocode.fixed-delay-ms}",
            initialDelayString = "${festival.geocode.initial-delay-ms}")
    public void runGeocode() {
        festivalGeocodeService.geocodeBatch(100); // 최대 100건
    }
}
