package com.teckit.festival.service;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.response.FestivalDetailDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.enumeration.FestivalScheduleDay;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.kafka.FestivalKafkaProducer;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.repository.FestivalScheduleRepository;
import com.teckit.festival.util.DateUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalManageService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository detailRepository;
    private final FestivalScheduleRepository scheduleRepository;
    private final FestivalKafkaProducer kafkaProducer;

    // 공연 등록 (기본정보 + 상세정보 + 일정)
    @Transactional
    public FestivalDetailDTO registerFestivalWithDetails(FestivalRegisterDTO request, Long userId) { // 👈 반환 타입 변경
        String fid = generateUniqueFid();

        var detailReq = request.getDetail();
        if (detailReq == null) throw new IllegalArgumentException("detail is required");

        int safeTicketPick  = Math.max(1, detailReq.getTicketPick());
        int safeMaxPurchase = Math.max(1, detailReq.getMaxPurchase());
        int safeAvailable   = Math.max(0, detailReq.getAvailableNOP());

        LocalDate fdfrom = DateUtil.parseDate(request.getFdfrom());
        LocalDate fdto   = DateUtil.parseDate(request.getFdto());
        String computedState = calcState(fdfrom, fdto);

        FestivalDetail detail = FestivalDetail.builder()
                .id(fid)
                .userId(userId)
                .fcltyid(detailReq.getFcltyid())
                .fname(request.getFname())
                .fdfrom(fdfrom)
                .fdto(fdto)
                .fcltynm(request.getFcltynm())
                .fcast(detailReq.getFcast())
                .story(detailReq.getStory())
                .ticketPrice(detailReq.getTicketPrice())
                .genrenm(request.getGenrenm())
                .fstate(computedState)
                .availableNOP(safeAvailable)
                .views(0)
                .faddress(detailReq.getFaddress())
                .ticketPick(safeTicketPick)
                .maxPurchase(safeMaxPurchase)
                .prfage(detailReq.getPrfage())
                .posterFile(request.getPosterFile())
                .contentFile(detailReq.getContentFile())
                .entrpsnmH(detailReq.getEntrpsnmH())
                .runningTime(detailReq.getRunningTime())
                .updatedate( (detailReq.getUpdatedate()!=null && !detailReq.getUpdatedate().isBlank())
                        ? LocalDateTime.parse(detailReq.getUpdatedate().substring(0,19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : LocalDateTime.now())
                .build();

        List<FestivalSchedule> schedules = (request.getSchedules()==null ? List.<FestivalSchedule>of()
                : request.getSchedules().stream()
                .map(s -> {
                    FestivalSchedule fs = FestivalSchedule.builder()
                            .festivalDetail(detail)
                            .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek().toUpperCase()))
                            .time(s.getTime())
                            .build();
                    return fs;
                })
                .collect(Collectors.toList()));
        detail.setSchedules(schedules);

        Festival festival = Festival.builder()
                .fname(request.getFname())
                .fdfrom(fdfrom)
                .fdto(fdto)
                .posterFile(request.getPosterFile())
                .fcltynm(request.getFcltynm())
                .genrenm(request.getGenrenm())
                .fstate(computedState)
                .prfage(detailReq.getPrfage())
                .festivalDetail(detail)
                .build();
        detail.setFestival(festival);

        detailRepository.saveAndFlush(detail);

        FestivalDetail persisted = detailRepository.findById(fid)
                .orElseThrow(() -> new IllegalStateException("Saved detail not found: " + fid));

        // 전송 대상: 저장 후 객체(persisted)
        kafkaProducer.send(persisted, "FESTIVAL_CREATED");

        // FestivalDetailDTO로 변환하여 반환
        return FestivalDetailDTO.fromEntity(persisted);
    }

    // 공연 수정
    @Transactional
    public FestivalDetailDTO updateFestival(String fid, FestivalRegisterDTO request, Long userId) { // 👈 반환 타입 변경
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        // 본인 소유 공연인지 확인
        if (!festival.getFestivalDetail().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_OWNER);
        }

        var detailReq = request.getDetail();
        if (detailReq == null) {
            throw new IllegalArgumentException("detail is required");
        }

        LocalDate fdfrom = DateUtil.parseDate(request.getFdfrom());
        LocalDate fdto   = DateUtil.parseDate(request.getFdto());
        String computedState = calcState(fdfrom, fdto);

        FestivalDetail detail = festival.getFestivalDetail();
        detail.setFname(request.getFname());
        detail.setFdfrom(fdfrom);
        detail.setFdto(fdto);
        detail.setFcltynm(request.getFcltynm());
        detail.setPosterFile(request.getPosterFile());
        detail.setGenrenm(request.getGenrenm());
        detail.setFstate(computedState);
        detail.setPrfage(detailReq.getPrfage());
        detail.setFaddress(detailReq.getFaddress());
        detail.setTicketPick(Math.max(1, detailReq.getTicketPick()));
        detail.setMaxPurchase(Math.max(1, detailReq.getMaxPurchase()));
        detail.setTicketPrice(detailReq.getTicketPrice());
        detail.setUpdatedate(
                detailReq.getUpdatedate() != null && !detailReq.getUpdatedate().isBlank()
                        ? LocalDateTime.parse(
                        detailReq.getUpdatedate().substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                        : LocalDateTime.now()
        );

        List<FestivalSchedule> schedules = (request.getSchedules() == null ? List.<FestivalSchedule>of()
                : request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail)
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek().toUpperCase()))
                        .time(s.getTime())
                        .build())
                .collect(Collectors.toList()));
        detail.setSchedules(schedules);

        festival.setFname(request.getFname());
        festival.setFdfrom(fdfrom);
        festival.setFdto(fdto);
        festival.setPosterFile(request.getPosterFile());
        festival.setFcltynm(request.getFcltynm());
        festival.setGenrenm(request.getGenrenm());
        festival.setPrfage(detailReq.getPrfage());
        festival.setFstate(computedState);

        festivalRepository.saveAndFlush(festival);
        Hibernate.initialize(festival.getFestivalDetail().getSchedules());

        kafkaProducer.send(festival.getFestivalDetail(), "FESTIVAL_UPDATED");

        // FestivalDetailDTO로 변환하여 반환
        return FestivalDetailDTO.fromEntity(festival.getFestivalDetail());
    }

    // 공연 삭제
    @Transactional
    public void deleteFestivalByHost(String fid, Long userId, boolean isAdmin) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        if (!isAdmin && !festival.getFestivalDetail().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_OWNER);
        }

        kafkaProducer.sendDeleted(fid);

        festivalRepository.delete(festival);
        detailRepository.deleteById(fid);
    }

    // 공연 목록 조회
    public List<FestivalDTO> getFestivalsByRole(Long userId, boolean isAdmin) {
        List<Festival> festivals;
        if (isAdmin) {
            festivals = festivalRepository.findAll();
        } else {
            festivals = festivalRepository.findByFestivalDetail_UserId(userId);
        }
        return festivals.stream()
                .map(FestivalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // fid(PF000001) 자동 생성
    private String generateUniqueFid() {
        String fid;
        do {
            fid = "PF" + randomNumeric(6);
        } while (detailRepository.existsById(fid));
        return fid;
    }

    private String randomNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append((int) (Math.random() * 10));
        }
        return builder.toString();
    }

    // 공연 상태 계산
    private String calcState(LocalDate fdfrom, LocalDate fdto) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        if (fdfrom == null || fdto == null) return "공연예정";
        if (today.isBefore(fdfrom)) return "공연예정";
        if (today.isAfter(fdto))   return "공연완료";
        return "공연중";
    }
}