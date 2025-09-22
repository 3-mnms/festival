package com.teckit.festival.enumeration;

import java.time.DayOfWeek;

public enum FestivalScheduleDay {
    MON("월"),
    TUE("화"),
    WED("수"),
    THU("목"),
    FRI("금"),
    SAT("토"),
    SUN("일");
    //HOL("공휴일");

    private final String label;

    FestivalScheduleDay(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static FestivalScheduleDay fromKorean(String kor) {
        for (FestivalScheduleDay day : values()) {
            if (kor.startsWith(day.label)) return day;
        }
        throw new IllegalArgumentException("지원하지 않는 요일: " + kor);
    }

    // DayOfWeek를 FestivalScheduleDay로 변환하는 메서드 수정
    public static FestivalScheduleDay fromDayOfWeek(DayOfWeek dayOfWeek) {
        return FestivalScheduleDay.valueOf(dayOfWeek.name().substring(0, 3));
    }
}