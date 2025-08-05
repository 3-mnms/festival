package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalDTO {

    // KoPIS API 기본 정보
    private String mt20id;      // 공연 ID
    private String prfnm;       // 공연명
    private String prfpdfrom;   // 공연 시작일 (yyyy.MM.dd)
    private String prfpdto;     // 공연 종료일 (yyyy.MM.dd)
    private String poster;      // 포스터 URL
    private String area;        // 지역명
    private String fcltynm;     // 공연장명
    private String genrenm;     // 장르명
    private String openrun;     // 오픈런 여부
    private String prfstate;    // 공연 상태

    // 내부 DB 저장용 필드
    private String id;          // 커스텀 공연 ID (mt20id 대체 가능)
    private Long hid;           // 주최자 ID (Host ID)
    private String fname;       // 공연명 (DB용)
    private LocalDate fdfrom;   // 공연 시작일 (LocalDate)
    private LocalDate fdto;     // 공연 종료일 (LocalDate)
    private String fcltyid;     // 공연장 ID
    private String fcast;       // 출연진
    private String fage;        // 관람연령
    private int ticketPrice;    // 티켓 가격
    private String story;       // 공연 설명
    private List<String> styurls; // 스타일 URL 리스트

    // Entity 변환 메서드
    public Festival toEntity() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return Festival.builder()
                .id(mt20id != null ? mt20id : id != null ? id : generateCustomId())  // KoPIS ID → DB ID → 랜덤 ID
                .fname(prfnm != null ? prfnm : fname)  // KoPIS 공연명 → DB 공연명
                .fdfrom(prfpdfrom != null ? LocalDate.parse(prfpdfrom, formatter) : fdfrom)  // KoPIS 날짜 → DB 날짜
                .fdto(prfpdto != null ? LocalDate.parse(prfpdto, formatter) : fdto)
                .poster(poster)
                .area(area)
                .fcltynm(fcltynm)
                .genrename(genrenm)  // ⛔️ [오타] 'genrename' → 'genrenm' 으로 Entity 확인 필요
                .fstate(prfstate != null ? prfstate : "READY")  // 상태 없으면 READY로 기본값
                .build();
    }

    // 공연 ID가 없을 때 랜덤 생성 (PF + 6자리 랜덤)
    private String generateCustomId() {
        return "PF" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}