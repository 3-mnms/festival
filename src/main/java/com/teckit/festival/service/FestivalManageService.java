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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.teckit.festival.kafka.FestivalKafkaProducer;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalManageService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository detailRepository;
    private final FestivalScheduleRepository scheduleRepository;

    private final FestivalKafkaProducer kafkaProducer;

    /**
     * 공연 등록 (기본정보 + 상세정보 + 일정)
     */
    @Transactional
    public String registerFestivalWithDetails(FestivalRegisterDTO request) {
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
                .loginId(request.getLoginId())
                .fcltyid(detailReq.getFcltyid())
                .fname(request.getFname())
                .fdfrom(request.getFdfrom())
                .fdto(request.getFdto())
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
                .build();

        List<FestivalSchedule> schedules = (request.getSchedules() == null ? List.<FestivalSchedule>of()
                : request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail)
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek().toUpperCase()))
                        .time(s.getTime())
                        .build())
                .collect(Collectors.toList()));
        detail.setSchedules(schedules);

        Festival festival = Festival.builder()
                //.loginId(request.getLoginId())   // ✅ 필수
                .fname(request.getFname())
                .fdfrom(request.getFdfrom())
                .fdto(request.getFdto())
                .posterFile(request.getPosterFile())
                .fcltynm(request.getFcltynm())
                .genrenm(request.getGenrenm())
                .fstate("공연예정")
                .festivalDetail(detail)
                .build();
        detail.setFestival(festival);

        detailRepository.save(detail);

        kafkaProducer.send(detail);

        return fid;
    }

    /**
     * 공연 수정
     */
    @Transactional
    public Festival updateFestival(String fid, FestivalDTO dto) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        FestivalDetail detail = festival.getFestivalDetail();
        if (detail != null) {
            detail.setFname(dto.getPrfnm());
            detail.setFdfrom(dto.getPrfpdfrom());
            detail.setFdto(dto.getPrfpdto());
            detail.setFcltynm(dto.getFcltynm());
            detail.setPosterFile(dto.getPoster());
            detail.setGenrenm(dto.getGenrenm());
            detail.setFstate(dto.getPrfstate());
            detail.setPrfage(dto.getPrfage());
            detail.setTicketPick(Math.max(1, dto.getTicketPick()));
            detail.setMaxPurchase(Math.max(1, dto.getMaxPurchase()));
            detail.setTicketPrice(dto.getTicketPrice());
        }

        festival.setFname(dto.getPrfnm());
        festival.setFdfrom(dto.getPrfpdfrom());
        festival.setFdto(dto.getPrfpdto());
        festival.setFcltynm(dto.getFcltynm());
        festival.setPosterFile(dto.getPoster());
        festival.setGenrenm(dto.getGenrenm());
        festival.setFstate(dto.getPrfstate());
        festival.setFage(dto.getPrfage());

        // 선택: 수정 후 카프카 전송
        kafkaProducer.send(festival.getFestivalDetail());

        return festivalRepository.save(festival);
    }

    /**
     * 공연 삭제 (주최자)
     */
    @Transactional
    public void deleteFestivalByHost(String fid, String loginId) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        if (!festival.getFestivalDetail().getLoginId().equals(loginId)) {
            throw new BusinessException(ErrorCode.NO_AUTHORITY);
        }

        // 1) Festival 먼저 삭제 (FK가 Festival 쪽이라 먼저 지워도 OK)
        festivalRepository.delete(festival);

        // 2) Detail 삭제
        detailRepository.deleteById(fid);
    }


    /**
     * 주최자 공연 목록 조회
     */
    public List<FestivalDTO> getFestivalsByHost(String loginId) {
        return festivalRepository.findByFestivalDetail_LoginId(loginId)
                .stream()
                .map(FestivalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 전체 공연 목록 조회 (관리자)
     */
    public List<FestivalDTO> getAllFestivals() {
        return festivalRepository.findAll()
                .stream()
                .map(FestivalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 공연 삭제 (관리자)
     */
    @Transactional
    public void adminDeleteFestival(String fid) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        festivalRepository.delete(festival);
    }

    /**
     * fid(PF000001) 자동 생성
     */
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
