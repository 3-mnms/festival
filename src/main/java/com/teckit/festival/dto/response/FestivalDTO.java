package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalDTO {

    private String mt20id;         // 공연 ID
    private String prfnm;          // 공연명
    private String prfpdfrom;      // 시작일
    private String prfpdto;        // 종료일
    private String fcltynm;        // 시설명
    private String poster;         // 포스터 이미지 경로
    private String area;           // 지역
    private String genrenm;        // 장르
    private String prfstate;       // 공연 상태
    private String prfage;         // 관람 연령

    // 🔽 추가 필드
    private String loginId;        // 주최자 ID
    private int ticketPick;        // 티켓 수령 방법
    private int maxPurchase;       // 티켓 구매 제한 개수
    private int ticketPrice;       // 티켓 가격
    private int availableNOP;      // 수용 가능 인원

    public Festival toEntity() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return Festival.builder()
                .fname(prfnm)
                .fdfrom(parseDate(prfpdfrom, formatter))
                .fdto(parseDate(prfpdto, formatter))
                .posterFile(poster)
                .area(area)
                .fcltynm(fcltynm)
                .genrenm(genrenm)
                .fstate(prfstate)
                .fage(prfage)
                .loginId(loginId)
                .ticketPick(ticketPick)
                .maxPurchase(maxPurchase)
                .ticketPrice(ticketPrice)
                .availableNOP(availableNOP)
                .build();
    }

    public static FestivalDTO fromEntity(Festival festival) {
        return FestivalDTO.builder()
                .mt20id(festival.getFestivalDetail().getId())
                .prfnm(festival.getFname())
                .prfpdfrom(festival.getFdfrom().toString())
                .prfpdto(festival.getFdto().toString())
                .fcltynm(festival.getFcltynm())
                .poster(festival.getPosterFile())
                .area(festival.getArea())
                .genrenm(festival.getGenrenm())
                .prfstate(festival.getFstate())
                .prfage(festival.getFage())
                .loginId(festival.getLoginId())
                .ticketPick(festival.getTicketPick())
                .maxPurchase(festival.getMaxPurchase())
                .ticketPrice(festival.getTicketPrice())
                .availableNOP(festival.getAvailableNOP())
                .build();
    }

    private LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        try {
            if (dateStr == null || dateStr.isBlank()) return null;
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            System.err.println("날짜 파싱 실패: " + dateStr);
            return null;
        }
    }
}