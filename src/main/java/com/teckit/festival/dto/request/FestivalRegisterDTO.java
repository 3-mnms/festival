package com.teckit.festival.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공연 등록 요청 DTO")
public class FestivalRegisterDTO {

    @Schema(description = "주최자 ID", example = "1")
    private Long hid;

    @Schema(description = "공연명", example = "뮤지컬 캣츠")
    private String fname;

    @Schema(description = "공연 시작일", example = "2025-08-06")
    private LocalDate fdfrom;

    @Schema(description = "공연 종료일", example = "2025-08-10")
    private LocalDate fdto;

    @Schema(description = "포스터 URL", example = "https://example.com/poster.jpg")
    private String poster;

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
    }
}
