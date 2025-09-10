package com.teckit.festival.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.teckit.festival.dto.FestivalKafkaDTO;
import com.teckit.festival.enumeration.GeocodeStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FestivalDetail {

    @Id
    @Column(length = 20)
    private String id;  // 예: PF132236

    @OneToOne(mappedBy = "festivalDetail", cascade = CascadeType.ALL)
    @JsonBackReference
    private Festival festival;

    @Column(nullable = false)
    private Long userId;

    private String fcltyid;
    private String fname;
    private LocalDate fdfrom;
    private LocalDate fdto;
    private String fcltynm;
    private String fcast;

    @Column(columnDefinition = "TEXT")
    private String story;

    private int ticketPrice;
    private String genrenm;
    private String fstate;
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedate;
    private int availableNOP;
    private int views;
    private String faddress;
    private int ticketPick;
    private int maxPurchase;
    private String prfage;
    private String posterFile;
    private String entrpsnmH;
    private String runningTime;

    @Column(name = "latitude",  columnDefinition = "DECIMAL(10,7)")
    private Double latitude; //위도

    @Column(name = "longitude",  columnDefinition = "DECIMAL(10,7)")
    private Double longitude;//경도

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GeocodeStatus isGeocoded = GeocodeStatus.PENDING;//지오코드 여부(위도, 경도)

    @OneToMany(mappedBy = "festivalDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FestivalReview> festivalReviews = new ArrayList<>();

    @OneToOne(mappedBy = "festivalDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private FestivalReviewAnalyze festivalReviewAnalyze;

    @Builder.Default
    @OneToMany(mappedBy = "festivalDetail", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<NearbyFestival> nearbyFestivals = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    private List<String> contentFile = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "festivalDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FestivalSchedule> schedules = new ArrayList<>();

    public void setSchedules(List<FestivalSchedule> schedules) {
        this.schedules.clear();
        if (schedules != null) {
            for (FestivalSchedule s : schedules) {
                s.setFestivalDetail(this);
            }
            this.schedules.addAll(schedules);
        }
    }

    // Kafka 전송용 변환 메서드
    public FestivalKafkaDTO toKafkaDTO() {
        List<FestivalKafkaDTO.ScheduleDTO> scheduleList =
                (this.schedules == null)
                        ? new ArrayList<>()
                        : this.schedules.stream()
                        .map(s -> FestivalKafkaDTO.ScheduleDTO.builder()
                                .dayOfWeek(s.getDayOfWeek().name())    // Enum → String
                                .time(s.getTime())                     // "12:00"
                                .build())
                        .collect(Collectors.toList());

        return FestivalKafkaDTO.builder()
                .id(this.id)
                .userId(this.userId)
                .fname(this.fname)
                .fdfrom(this.fdfrom)
                .fdto(this.fdto)
                .posterFile(this.posterFile)
                .fcltynm(this.fcltynm)
                .ticketPick(this.ticketPick)
                .maxPurchase(this.maxPurchase)
                .ticketPrice(this.ticketPrice)
                .availableNOP(this.availableNOP)
                .schedules(scheduleList)
                .build();
    }
}