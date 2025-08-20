package com.teckit.festival.util;

import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.enumeration.FestivalScheduleDay;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;

public class FestivalScheduleGenerator {

    private static final Random random = new Random();

    public static List<FestivalSchedule> generateRandomSchedules(
            FestivalDetail festivalDetail,
            LocalDate fdfrom, // 시작일
            LocalDate fdto    // 종료일
    ) {
        List<FestivalSchedule> result = new ArrayList<>();

        // 1. 공연 기간 내 존재하는 모든 요일 추출
        Set<DayOfWeek> availableDaysOfWeek = new HashSet<>();
        LocalDate currentDate = fdfrom;
        while (!currentDate.isAfter(fdto)) {
            availableDaysOfWeek.add(currentDate.getDayOfWeek());
            currentDate = currentDate.plusDays(1);
        }

        // 2. 추출된 요일 중 무작위로 2~4개 선택
        int numSchedulesToGenerate = random.nextInt(3) + 2; // 2~4개
        List<DayOfWeek> shuffledDays = new ArrayList<>(availableDaysOfWeek);
        Collections.shuffle(shuffledDays);
        List<DayOfWeek> selectedDays = new ArrayList<>();

        // 공연 기간이 2~4일 미만인 경우
        if (shuffledDays.size() < numSchedulesToGenerate) {
            selectedDays = shuffledDays;
        } else {
            selectedDays = shuffledDays.subList(0, numSchedulesToGenerate);
        }

        // 3. 선택된 요일별로 무작위 시간 설정
        for (DayOfWeek day : selectedDays) {
            int hour = random.nextInt(9) + 12; // 12시~20시
            String time = String.format("%02d:00", hour);

            FestivalSchedule schedule = FestivalSchedule.builder()
                    // 수정된 FestivalScheduleDay의 fromDayOfWeek 메서드 사용
                    .dayOfWeek(FestivalScheduleDay.fromDayOfWeek(day))
                    .time(time)
                    .build();

            // 연관관계 주입
            schedule.setFestivalDetail(festivalDetail);

            result.add(schedule);
        }

        return result;
    }

    public static int generateRandomPrice() {
        return random.nextInt(1, 11) * 10000;
    }

    public static int generateRandomAvailableNOP() {
        return random.nextInt(1, 11) * 100;
    }
}