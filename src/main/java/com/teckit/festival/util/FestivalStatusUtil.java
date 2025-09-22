package com.teckit.festival.util;

import java.time.LocalDate;
import java.time.ZoneId;

public class FestivalStatusUtil {

    public static String calcState(LocalDate fdfrom, LocalDate fdto) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        if (fdfrom == null || fdto == null) return "공연예정";
        if (today.isBefore(fdfrom)) return "공연예정";
        if (today.isAfter(fdto)) return "공연완료";
        return "공연중";
    }
}
