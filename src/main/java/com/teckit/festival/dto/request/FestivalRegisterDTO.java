package com.teckit.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공연 등록 요청 DTO")
public class FestivalRegisterDTO {

    @Schema(description = "주최자 ID", example = "host01")
    private String loginId;

    @Schema(description = "공연명", example = "뮤지컬 캣츠")
    private String fname;

    @Schema(description = "공연 시작일", example = "2025-08-06")
    private String fdfrom;

    @Schema(description = "공연 종료일", example = "2025-08-10")
    private String fdto;

    @Schema(description = "포스터 이미지 URL", example = "https://example.com/poster.jpg")
    private String posterFile;

    @Schema(description = "공연 지역", example = "서울")
    private String area;

    @Schema(description = "공연장 이름", example = "세종문화회관")
    private String fcltynm;

    @Schema(description = "장르명", example = "뮤지컬")
    private String genrenm;

    @Schema(description = "공연 상세 정보")
    private FestivalDetailDTO detail;

    @Schema(description = "공연 일정 리스트")
    private List<FestivalScheduleDTO> schedules;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "공연 상세정보 DTO")
    public static class FestivalDetailDTO {

        @Schema(description = "공연장 ID", example = "FC123456")
        private String fcltyid;

        @Schema(description = "출연진", example = "홍길동, 김철수")
        private String fcast;

        @Schema(description = "줄거리", example = "고양이들의 화려한 쇼!")
        private String story;

        @Schema(description = "티켓 가격", example = "80000")
        private int ticketPrice;

        @Schema(description = "공연장 주소", example = "서울특별시 종로구 세종대로 175")
        private String faddress;

        @Schema(description = "티켓 수령 방식 (0: 일괄배송, 1: 현장수령, 2: 둘다)", example = "0")
        private int ticketPick;

        @Schema(description = "티켓 1인당 최대 구매 수량", example = "4")
        private int maxPurchase;

        @Schema(description = "관람 연령", example = "12")
        private int prfage;

        @Schema(description = "공연 상태", example = "공연예정")
        private String prfstate;

        @Schema(description = "수용 가능 인원", example = "300")
        private int availableNOP;

        @Schema(description = "상세 이미지 리스트", example = "[\"https://example.com/image1.jpg\"]")
        private List<String> contentFile;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "공연 일정 DTO")
    public static class FestivalScheduleDTO {
        @Schema(description = "요일", example = "MON")
        private String dayOfWeek;

        @Schema(description = "시간", example = "19:00")
        private String time;
    }
}
