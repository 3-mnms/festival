package com.teckit.festival.util;

import com.teckit.festival.enumeration.FestivalScheduleDay;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FestivalValidatorUtil {

    // 공연 날짜 검증
    public static void validateDates(LocalDate fdfrom, LocalDate fdto) {
        if (!fdfrom.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_FESTIVAL_DATE, "공연 시작일은 오늘 이후여야 합니다.");
        }
        if (fdto.isBefore(fdfrom)) {
            throw new BusinessException(ErrorCode.INVALID_FESTIVAL_DATE, "공연 종료일은 시작일과 같거나 이후여야 합니다.");
        }
    }

    // 공연 스케줄 요일 검증
    public static <T> void validateSchedules(List<T> schedules, LocalDate fdfrom, LocalDate fdto) {
        if (schedules == null || schedules.isEmpty()) return;

        Set<FestivalScheduleDay> validDays = fdfrom.datesUntil(fdto.plusDays(1))
                .map(LocalDate::getDayOfWeek)
                .map(FestivalScheduleDay::fromDayOfWeek)
                .collect(Collectors.toSet());

        for (var s : schedules) {
            try {
                // 리플렉션 대신 getter 메서드 이름을 직접 가정
                String dayOfWeek = (String) s.getClass().getMethod("getDayOfWeek").invoke(s);

                FestivalScheduleDay day = FestivalScheduleDay.valueOf(dayOfWeek.toUpperCase());
                if (!validDays.contains(day)) {
                    throw new BusinessException(
                            ErrorCode.INVALID_SCHEDULE_DAY,
                            "잘못된 스케줄 요일(" + day + ")입니다. 공연 기간에 포함되지 않습니다."
                    );
                }
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "스케줄 검증 중 오류가 발생했습니다.");
            }
        }
    }
}
