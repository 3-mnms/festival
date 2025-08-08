package com.teckit.festival.service;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.enumeration.FestivalScheduleDay;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.repository.FestivalScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalManageService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository detailRepository;
    private final FestivalScheduleRepository scheduleRepository;

    /**
     * 공연 등록 (기본정보 + 상세정보 + 일정)
     */
    @Transactional
    public String registerFestivalWithDetails(FestivalRegisterDTO request) {
        String fid = generateUniqueFid();

        // 상세정보 엔티티 생성
        FestivalDetail detail = FestivalDetail.builder()
                .id(fid)
                .loginId(request.getLoginId())
                .fcltyid(request.getDetail().getFcltyid())
                .fname(request.getFname())
                .fdfrom(request.getFdfrom())
                .fdto(request.getFdto())
                .fcltynm(request.getFcltynm())
                .fcast(request.getDetail().getFcast())
                .story(request.getDetail().getStory())
                .ticketPrice(request.getDetail().getTicketPrice())
                .genrenm(request.getGenrenm())
                .fstate("공연예정")
                .availableNOP(0)
                .views(0)
                .faddress(request.getDetail().getFaddress())
                .ticketPick(request.getDetail().getTicketPick())
                .maxPurchase(request.getDetail().getMaxPurchase())
                .contentFile(request.getDetail().getContentFile())
                .build();

        // 공연 일정 엔티티 리스트 생성
        List<FestivalSchedule> schedules = request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail)
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek()))
                        .time(s.getTime())
                        .build())
                .collect(Collectors.toList());

        detail.setSchedules(schedules);

        // 공연 기본정보 엔티티 생성
        Festival festival = Festival.builder()
                .loginId(request.getLoginId())
                .fname(request.getFname())
                .fdfrom(request.getFdfrom())
                .fdto(request.getFdto())
                .posterFile(request.getPosterFile())
                .area(request.getArea())
                .fcltynm(request.getFcltynm())
                .genrenm(request.getGenrenm())
                .fstate("공연예정")
                .festivalDetail(detail)
                .build();

        detail.setFestival(festival);

        // 저장 (Cascade로 festival, schedules 함께 저장)
        detailRepository.save(detail);

        return fid;
    }

    /**
     * 공연 수정
     */
    @Transactional
    public Festival updateFestival(String fid, FestivalDTO dto) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        festival.setFname(dto.getPrfnm());
        festival.setFdfrom(dto.getPrfpdfrom());
        festival.setFdto(dto.getPrfpdto());
        festival.setFcltynm(dto.getFcltynm());
        festival.setPosterFile(dto.getPoster());
        festival.setArea(dto.getArea());
        festival.setGenrenm(dto.getGenrenm());

        return festivalRepository.save(festival);
    }

    /**
     * 공연 삭제 (주최자)
     */
    @Transactional
    public void deleteFestivalByHost(String fid, String loginId) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        if (!festival.getLoginId().equals(loginId)) {
            throw new BusinessException(ErrorCode.NO_AUTHORITY);
        }

        festivalRepository.delete(festival);
    }

    /**
     * 주최자 공연 목록 조회
     */
    public List<FestivalDTO> getFestivalsByHost(String loginId) {
        return festivalRepository.findByLoginId(loginId)
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
