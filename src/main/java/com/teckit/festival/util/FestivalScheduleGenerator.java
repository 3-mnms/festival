package com.teckit.festival.util;

import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.enumeration.FestivalScheduleDay;

import java.util.*;

public class FestivalScheduleGenerator {

    private static final FestivalScheduleDay[] days=FestivalScheduleDay.values();
    private static final Random random=new Random();

    public static List<FestivalSchedule> generateRandomSchedules(FestivalDetail festivalDetail){
        List<FestivalSchedule> result=new ArrayList<>();

//        0,1,2 -> 2,3,4
        int numDays= random.nextInt(3)+2;

        Set<FestivalScheduleDay> selectedDays=new HashSet<>();

        while(selectedDays.size()<numDays){
//            무작위 요일 발생
            FestivalScheduleDay day=days[random.nextInt(7)];
            selectedDays.add(day);
        }

        for (FestivalScheduleDay day:selectedDays){
            int hour = random.nextInt(9) + 12; // 12시~20시
            String time = String.format("%02d:00", hour);
            result.add(FestivalSchedule.builder()
                    .festivalDetail(festivalDetail)
                    .dayOfWeek(day)
                    .time(time)
                    .build());
        }

        return result;
    }

    public static int generateRandomPrice(){
        return random.nextInt(1,11)*10000;
    }

    public static int generateRandomAvailableNOP(){
        return random.nextInt(1,11)*100;
    }
}