// 공연 관리용 응답 DTO
package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.util.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공연 등록 응답 DTO")
public class FestivalRegisterResponseDTO {

    @Schema(description = "공연 ID", example = "PF223122")
    private String fid;

    @Schema(description = "공연명", example = "뮤지컬 캣츠")
    private String fname;

    @Schema(description = "공연 시작일", example = "2025-08-06")
    private String fdfrom;

    @Schema(description = "공연 종료일", example = "2025-08-10")
    private String fdto;

    @Schema(description = "포스터 이미지 URL", example = "https://example.com/poster.jpg")
    private String posterFile;

    @Schema(description = "공연장 이름", example = "세종문화회관")
    private String fcltynm;

    @Schema(description = "장르명", example = "뮤지컬")
    private String genrenm;

    @Schema(description = "공연 상세 정보")
    private FestivalDetailDTO detail;

    @Schema(description = "공연 일정 리스트")
    private List<FestivalScheduleDTO> schedules;

    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static FestivalRegisterResponseDTO fromEntity(Festival festival, FestivalDetail detail, List<FestivalSchedule> schedules) {
        List<FestivalScheduleDTO> scheduleDTOs = schedules.stream()
                .map(FestivalScheduleDTO::fromEntity)
                .collect(Collectors.toList());

        return FestivalRegisterResponseDTO.builder()
                .fid(detail.getId())
                .fname(festival.getFname())
                .fdfrom(DateUtil.formatDate(festival.getFdfrom()))
                .fdto(DateUtil.formatDate(festival.getFdto()))
                .posterFile(festival.getPosterFile())
                .fcltynm(festival.getFcltynm())
                .genrenm(festival.getGenrenm())
                .detail(FestivalDetailDTO.fromEntity(detail))
                .schedules(scheduleDTOs)
                .build();
    }

    //---------------------------------------------------------
    // 내부 클래스
    //---------------------------------------------------------

    @Getter
    @Setter
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

        @Schema(description = "티켓 수령 방식 (1: 현장수령, 2: 배송, 3: 둘다)", example = "1")
        private int ticketPick;

        @Schema(description = "티켓 1인당 최대 구매 수량", example = "4")
        private int maxPurchase;

        @Schema(description = "관람 연령", example = "만 12세 이상")
        private String prfage;

        @Schema(description = "수용 가능 인원", example = "300")
        private int availableNOP;

        @Schema(description = "상세 이미지 리스트", example = "[\"https://example.com/image1.jpg\"]")
        private List<String> contentFile = new ArrayList<>();

        @Schema(description = "최종 수정일", example = "2025-08-11 10:03:14")
        private String updatedate;

        @Schema(description = "주최명", example = "서초구청")
        private String entrpsnmH;

        @Schema(description = "러닝 타임", example = "1시간 30분")
        private String runningTime;

        public static FestivalDetailDTO fromEntity(FestivalDetail entity) {
            return FestivalDetailDTO.builder()
                    .fcltyid(entity.getFcltyid())
                    .fcast(entity.getFcast())
                    .story(entity.getStory())
                    .ticketPrice(entity.getTicketPrice())
                    .faddress(entity.getFaddress())
                    .ticketPick(entity.getTicketPick())
                    .maxPurchase(entity.getMaxPurchase())
                    .prfage(entity.getPrfage())
                    .availableNOP(entity.getAvailableNOP())
                    .contentFile(entity.getContentFile())
                    .updatedate(entity.getUpdatedate() != null ? entity.getUpdatedate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null)
                    .entrpsnmH(entity.getEntrpsnmH())
                    .runningTime(entity.getRunningTime())
                    .build();
        }
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

        public static FestivalScheduleDTO fromEntity(FestivalSchedule entity) {
            return FestivalScheduleDTO.builder()
                    .dayOfWeek(entity.getDayOfWeek().name())
                    .time(entity.getTime())
                    .build();
        }
    }
}