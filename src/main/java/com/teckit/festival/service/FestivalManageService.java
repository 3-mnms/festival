package com.teckit.festival.service;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.request.FestivalScheduleDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.enumeration.FestivalScheduleDay;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.mapper.FestivalScheduleMapper;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.repository.FestivalScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FestivalManageService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository detailRepository;
    private final FestivalScheduleRepository scheduleRepository;
    private final FestivalScheduleMapper festivalScheduleMapper;

    @Transactional
    public String registerFestivalWithDetails(FestivalRegisterDTO request) {
        String festivalId = generateUniqueFestivalId();

        Festival festival = Festival.builder()
                .id(festivalId)
                .hid(request.getHid())
                .fname(request.getFname())
                .fdfrom(request.getFdfrom())
                .fdto(request.getFdto())
                .poster(request.getPoster())
                .area(request.getArea())
                .fcltynm(request.getFcltynm())
                .genrenm(request.getGenrenm())
                .fstate("공연예정")
                .build();

        FestivalDetail detail = FestivalDetail.builder()
                .festival(festival)
                .fcltyid(request.getDetail().getFcltyid())
                .fname(request.getFname())
                .fdfrom(request.getFdfrom().toString())
                .fdto(request.getFdto().toString())
                .fcltynm(request.getFcltynm())
                .fcast(request.getDetail().getFcast())
                .story(request.getDetail().getStory())
                .ticketPrice(request.getDetail().getTicketPrice())
                .genrenm(request.getGenrenm())
                .fstate("공연예정")
                .visit("0")
                .availableNOP(0)
                .updatedate(LocalDate.now().toString())
                .views(0)
                .build();

        List<FestivalSchedule> schedules = request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail)
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek()))
                        .time(s.getTime())
                        .build())
                .collect(Collectors.toList());

        detail.setSchedules(schedules);
        festival.setFestivalDetail(detail);

        festivalRepository.save(festival);  // cascade 로 detail, schedule도 같이 저장됨

        return festivalId;
    }

    @Transactional
    public Festival updateFestival(String fid, FestivalDTO dto) {
        Festival festival = festivalRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        festival.setFname(dto.getPrfnm());
        festival.setFdfrom(LocalDate.parse(dto.getPrfpdfrom()));
        festival.setFdto(LocalDate.parse(dto.getPrfpdto()));
        festival.setFcltynm(dto.getFcltynm());
        festival.setPoster(dto.getPoster());
        festival.setArea(dto.getArea());
        festival.setGenrenm(dto.getGenrenm());

        return festivalRepository.save(festival);
    }

    @Transactional
    public void deleteFestivalByHost(String fid, Long hostId) {
        Festival festival = festivalRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        if (!festival.getHid().equals(hostId)) {
            throw new BusinessException(ErrorCode.NO_AUTHORITY);
        }

        festivalRepository.delete(festival);
    }

    public List<Festival> getFestivalsByHost(Long hostId) {
        return festivalRepository.findByHid(hostId);
    }

    public List<Festival> getAllFestivals() {
        return festivalRepository.findAll();
    }

    @Transactional
    public void adminDeleteFestival(String fid) {
        Festival festival = festivalRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        festivalRepository.delete(festival);
    }

    private String generateUniqueFestivalId() {
        String id;
        do {
            id = "PF" + randomNumeric(6);
        } while (festivalRepository.existsById(id));
        return id;
    }

    private String randomNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int digit = (int)(Math.random() * 10);  // 0~9
            builder.append(digit);
        }
        return builder.toString();
    }
}