package com.teckit.festival.service;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
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

import java.time.LocalDateTime;
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
    public String registerFestivalWithDetails(FestivalRegisterDTO request, String loginId) { // ** (파라미터에 loginId 추가) **
        String fid = generateUniqueFid();

        var detailReq = request.getDetail();
        if (detailReq == null) {
            throw new IllegalArgumentException("detail is required");
        }

        int safeTicketPick  = Math.max(1, detailReq.getTicketPick());
        int safeMaxPurchase = Math.max(1, detailReq.getMaxPurchase());
        int safeAvailable   = Math.max(0, detailReq.getAvailableNOP());

        FestivalDetail detail = FestivalDetail.builder()
                .id(fid)
                .loginId(loginId)
                .fcltyid(detailReq.getFcltyid())
                .fname(request.getFname())
                .fdfrom(DateUtil.parseDate(request.getFdfrom()))
                .fdto(DateUtil.parseDate(request.getFdto()))
                .fcltynm(request.getFcltynm())
                .fcast(detailReq.getFcast())
                .story(detailReq.getStory())
                .ticketPrice(detailReq.getTicketPrice())
                .genrenm(request.getGenrenm())
                .fstate("공연예정")
                .availableNOP(safeAvailable)
                .views(0)
                .faddress(detailReq.getFaddress())
                .ticketPick(safeTicketPick)
                .maxPurchase(safeMaxPurchase)
                .prfage(detailReq.getPrfage())
                .posterFile(request.getPosterFile())
                .contentFile(detailReq.getContentFile())
                .updatedate(
                        detailReq.getUpdatedate() != null && !detailReq.getUpdatedate().isBlank()
                                ? LocalDateTime.parse(
                                detailReq.getUpdatedate().substring(0, 19),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        )
                                : LocalDateTime.now()
                )
                .build();

        List<FestivalSchedule> schedules = (request.getSchedules() == null ? List.<FestivalSchedule>of()
                : request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail) // 등록 시 festivalDetailId 무시
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek().toUpperCase()))
                        .time(s.getTime())
                        .build())
                .collect(Collectors.toList()));
        detail.setSchedules(schedules);

        Festival festival = Festival.builder()
                .fname(request.getFname())
                .fdfrom(DateUtil.parseDate(request.getFdfrom()))
                .fdto(DateUtil.parseDate(request.getFdto()))
                .posterFile(request.getPosterFile())
                .fcltynm(request.getFcltynm())
                .genrenm(request.getGenrenm())
                .fstate("공연예정")
                .festivalDetail(detail)
                .build();
        detail.setFestival(festival);

        detailRepository.saveAndFlush(detail);
        Hibernate.initialize(detail.getSchedules());

        kafkaProducer.send(detail, "FESTIVAL_CREATED");

        return fid;
    }

    // 공연 수정
    @Transactional
    public Festival updateFestival(String fid, FestivalRegisterDTO request, String loginId) { // ** (파라미터에 loginId 추가) **
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        // 본인 소유 공연인지 확인
        if (!festival.getFestivalDetail().getLoginId().equals(loginId)) { // ** (권한 체크 추가) **
            throw new BusinessException(ErrorCode.NO_AUTHORITY);
        }

        var detailReq = request.getDetail();
        if (detailReq == null) {
            throw new IllegalArgumentException("detail is required");
        }

        FestivalDetail detail = festival.getFestivalDetail();
        detail.setFname(request.getFname());
        detail.setFdfrom(DateUtil.parseDate(request.getFdfrom()));
        detail.setFdto(DateUtil.parseDate(request.getFdto()));
        detail.setFcltynm(request.getFcltynm());
        detail.setPosterFile(request.getPosterFile());
        detail.setGenrenm(request.getGenrenm());
        detail.setFstate("공연예정");
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
        festival.setFdfrom(DateUtil.parseDate(request.getFdfrom()));
        festival.setFdto(DateUtil.parseDate(request.getFdto()));
        festival.setPosterFile(request.getPosterFile());
        festival.setFcltynm(request.getFcltynm());
        festival.setGenrenm(request.getGenrenm());
        festival.setFstate("공연예정");

        festivalRepository.saveAndFlush(festival);
        Hibernate.initialize(festival.getFestivalDetail().getSchedules());

        kafkaProducer.send(festival.getFestivalDetail(), "FESTIVAL_UPDATED");

        return festivalRepository.save(festival);
    }

    // 공연 삭제 (주최자)
    @Transactional
    public void deleteFestivalByHost(String fid, String loginId) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        String owner = festival.getFestivalDetail().getLoginId();
        if (!owner.equals(loginId)) {
            throw new BusinessException(ErrorCode.NO_AUTHORITY);
        }

        kafkaProducer.sendDeleted(fid);

        festivalRepository.delete(festival);
        detailRepository.deleteById(fid);
    }

    // 주최자 공연 목록 조회
    public List<FestivalDTO> getFestivalsByHost(String loginId) {
        return festivalRepository.findByFestivalDetail_LoginId(loginId)
                .stream()
                .map(FestivalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 전체 공연 목록 조회 (관리자)
    public List<FestivalDTO> getAllFestivals() {
        return festivalRepository.findAll()
                .stream()
                .map(FestivalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 공연 삭제 (관리자)
    @Transactional
    public void adminDeleteFestival(String fid) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        String owner = festival.getFestivalDetail().getLoginId();

        kafkaProducer.sendDeleted(fid);

        festivalRepository.delete(festival);
        detailRepository.deleteById(fid);
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
}
