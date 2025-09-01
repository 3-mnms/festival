package com.teckit.festival.service;

import com.teckit.festival.dto.request.FestivalRegisterDTO;
import com.teckit.festival.dto.response.FestivalRegisterResponseDTO;
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
import com.teckit.festival.util.FestivalIdGenerator;
import com.teckit.festival.util.FestivalStatusUtil;
import com.teckit.festival.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FestivalManageService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository detailRepository;
    private final FestivalScheduleRepository scheduleRepository;
    private final FestivalKafkaProducer kafkaProducer;
    private final FestivalIdGenerator festivalIdGenerator;

    // 필드 주입
    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    // 공연 등록
    @Transactional
    public FestivalRegisterResponseDTO registerFestivalWithDetails(
            FestivalRegisterDTO request,
            Long userId,
            MultipartFile posterFile,
            List<MultipartFile> contentFiles
    ) {
        var detailReq = request.getDetail();
        if (detailReq == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "공연 상세 정보가 누락되었습니다.");
        }

        // 파일 저장 (FileUploadUtil만 사용)
        String posterUrl = FileUploadUtil.saveFile(posterFile, uploadDir, baseUrl, "pfmPoster");
        List<String> contentUrls = FileUploadUtil.saveFiles(contentFiles, uploadDir, baseUrl, "pfmIntroImage");

        String fid = festivalIdGenerator.generateUniqueFid();
        int safeTicketPick = Math.max(1, detailReq.getTicketPick());
        int safeMaxPurchase = Math.max(1, detailReq.getMaxPurchase());
        int safeAvailable = Math.max(0, detailReq.getAvailableNOP());

        LocalDate fdfrom = DateUtil.parseDate(request.getFdfrom());
        LocalDate fdto = DateUtil.parseDate(request.getFdto());
        String computedState = FestivalStatusUtil.calcState(fdfrom, fdto);

        FestivalDetail detail = FestivalDetail.builder()
                .id(fid)
                .userId(userId)
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
                .posterFile(posterUrl)
                .contentFile(contentUrls)
                .entrpsnmH(detailReq.getEntrpsnmH())
                .runningTime(detailReq.getRunningTime())
                .updatedate(LocalDateTime.now())
                .build();

        List<FestivalSchedule> schedules = (request.getSchedules() == null ? List.of()
                : request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail)
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek().toUpperCase()))
                        .time(s.getTime())
                        .build())
                .toList());
        detail.setSchedules(schedules);

        Festival festival = Festival.builder()
                .fname(request.getFname())
                .fdfrom(fdfrom)
                .fdto(fdto)
                .posterFile(posterUrl)
                .fcltynm(request.getFcltynm())
                .genrenm(request.getGenrenm())
                .fstate(computedState)
                .prfage(detailReq.getPrfage())
                .festivalDetail(detail)
                .build();
        detail.setFestival(festival);

        detailRepository.saveAndFlush(detail);

        FestivalDetail persisted = detailRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "저장된 공연 정보를 찾을 수 없습니다."));

        kafkaProducer.send(persisted, "FESTIVAL_CREATED");

        return FestivalRegisterResponseDTO.fromEntity(festival, persisted, schedules);
    }

    // 공연 수정
    @Transactional
    public FestivalRegisterResponseDTO updateFestival(
            String fid,
            FestivalRegisterDTO request,
            Long userId,
            MultipartFile posterFile,
            List<MultipartFile> contentFiles
    ) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        if (!festival.getFestivalDetail().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_OWNER);
        }

        var detailReq = request.getDetail();
        if (detailReq == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "공연 상세 정보가 누락되었습니다.");
        }

        // 기존 포스터 유지 or 교체
        String posterPath = (posterFile != null && !posterFile.isEmpty())
                ? FileUploadUtil.saveFile(posterFile, uploadDir, baseUrl, "pfmPoster")
                : festival.getPosterFile();

        // 기존 상세이미지 유지 or 교체
        List<String> contentPaths = (contentFiles != null && !contentFiles.isEmpty())
                ? FileUploadUtil.saveFiles(contentFiles, uploadDir, baseUrl, "pfmIntroImage")
                : festival.getFestivalDetail().getContentFile();

        LocalDate fdfrom = DateUtil.parseDate(request.getFdfrom());
        LocalDate fdto = DateUtil.parseDate(request.getFdto());
        String computedState = FestivalStatusUtil.calcState(fdfrom, fdto);

        FestivalDetail detail = festival.getFestivalDetail();
        detail.setFname(request.getFname());
        detail.setFdfrom(fdfrom);
        detail.setFdto(fdto);
        detail.setFcltynm(request.getFcltynm());
        detail.setPosterFile(posterPath);
        detail.setContentFile(contentPaths);
        detail.setGenrenm(request.getGenrenm());
        detail.setFstate(computedState);
        detail.setPrfage(detailReq.getPrfage());
        detail.setFaddress(detailReq.getFaddress());
        detail.setTicketPick(Math.max(1, detailReq.getTicketPick()));
        detail.setMaxPurchase(Math.max(1, detailReq.getMaxPurchase()));
        detail.setTicketPrice(detailReq.getTicketPrice());
        detail.setStory(detailReq.getStory());
        detail.setFcast(detailReq.getFcast());
        detail.setRunningTime(detailReq.getRunningTime());
        detail.setEntrpsnmH(detailReq.getEntrpsnmH());
        detail.setAvailableNOP(Math.max(0, detailReq.getAvailableNOP()));
        detail.setUpdatedate(
                detailReq.getUpdatedate() != null && !detailReq.getUpdatedate().isBlank()
                        ? LocalDateTime.parse(detailReq.getUpdatedate().substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : LocalDateTime.now()
        );

        // 스케줄 교체
        scheduleRepository.deleteByFestivalDetail_Id(fid);
        List<FestivalSchedule> schedules = (request.getSchedules() == null ? List.of()
                : request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail)
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek().toUpperCase()))
                        .time(s.getTime())
                        .build())
                .collect(Collectors.toList()));
        detail.setSchedules(schedules);
        scheduleRepository.saveAll(schedules);

        festival.setFname(request.getFname());
        festival.setFdfrom(fdfrom);
        festival.setFdto(fdto);
        festival.setPosterFile(posterPath);
        festival.setFcltynm(request.getFcltynm());
        festival.setGenrenm(request.getGenrenm());
        festival.setPrfage(detailReq.getPrfage());
        festival.setFstate(computedState);

        festivalRepository.saveAndFlush(festival);
        Hibernate.initialize(festival.getFestivalDetail().getSchedules());

        kafkaProducer.send(festival.getFestivalDetail(), "FESTIVAL_UPDATED");

        return FestivalRegisterResponseDTO.fromEntity(festival, detail, schedules);
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
    public List<FestivalRegisterResponseDTO> getFestivalsByRole(Long userId, boolean isAdmin) {
        List<Festival> festivals = isAdmin
                ? festivalRepository.findAll()
                : festivalRepository.findByFestivalDetail_UserId(userId);

        return festivals.stream()
                .map(festival -> {
                    Hibernate.initialize(festival.getFestivalDetail().getSchedules());
                    return FestivalRegisterResponseDTO.fromEntity(
                            festival,
                            festival.getFestivalDetail(),
                            festival.getFestivalDetail().getSchedules()
                    );
                })
                .toList();
    }

    // 공연 상세 조회
    @Transactional
    public FestivalRegisterResponseDTO getFestivalDetail(String fid, Long userId, boolean admin) {
        FestivalDetail detail = detailRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND, "존재하지 않는 공연입니다."));

        if (!admin && !detail.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_OWNER);
        }

        Hibernate.initialize(detail.getSchedules());
        return FestivalRegisterResponseDTO.fromEntity(detail.getFestival(), detail, detail.getSchedules());
    }

}