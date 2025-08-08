package com.teckit.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    // NOTE: 현재 Festival 엔티티에서 area를 쓰지 않으면 서비스에서 무시됨
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
        @Min(0)
        private int ticketPrice;

        @Schema(description = "공연장 주소", example = "서울특별시 종로구 세종대로 175")
        private String faddress;

        @Schema(description = "티켓 수령 방식 (1: 현장수령, 2: 배송, 3: 둘다)", example = "1")
        @Min(1) @Max(3)
        private int ticketPick;

        @Schema(description = "티켓 1인당 최대 구매 수량", example = "4")
        @Min(1) @Max(10)
        private int maxPurchase;

        // ✅ 엔티티는 String이므로 타입을 맞춰두는 걸 추천
        @Schema(description = "관람 연령(문자열)", example = "만 12세 이상")
        private String prfage;

        @Schema(description = "공연 상태", example = "공연예정")
        private String prfstate;

        @Schema(description = "수용 가능 인원", example = "300")
        @Min(0)
        private int availableNOP;

        @Schema(description = "상세 이미지 리스트", example = "[\"https://example.com/image1.jpg\"]")
        @Builder.Default
        private List<String> contentFile = new ArrayList<>();
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
