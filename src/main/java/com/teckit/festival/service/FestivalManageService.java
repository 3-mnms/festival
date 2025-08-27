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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FestivalManageService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository detailRepository;
    private final FestivalScheduleRepository scheduleRepository;
    private final FestivalKafkaProducer kafkaProducer;
    private final FileUploadService fileUploadService;

    @Transactional
    public FestivalRegisterResponseDTO registerFestivalWithDetails(FestivalRegisterDTO request, Long userId, MultipartFile posterFile, List<MultipartFile> contentFiles) {
        // 1. DTO 유효성 검사
        var detailReq = request.getDetail();
        if (detailReq == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "공연 상세 정보가 누락되었습니다.");
        }

        // 2. 파일 업로드
        String posterUrl = (posterFile != null && !posterFile.isEmpty()) ? fileUploadService.uploadFile(posterFile) : null;
        List<String> contentUrls = new ArrayList<>();
        if (contentFiles != null && !contentFiles.isEmpty()) {
            for (MultipartFile file : contentFiles) {
                contentUrls.add(fileUploadService.uploadFile(file));
            }
        }

        // 3. 고유한 ID 생성 (중복 체크 포함)
        String fid = generateUniqueFid();

        int safeTicketPick  = Math.max(1, detailReq.getTicketPick());
        int safeMaxPurchase = Math.max(1, detailReq.getMaxPurchase());
        int safeAvailable   = Math.max(0, detailReq.getAvailableNOP());

        LocalDate fdfrom = DateUtil.parseDate(request.getFdfrom());
        LocalDate fdto   = DateUtil.parseDate(request.getFdto());
        String computedState = calcState(fdfrom, fdto);

        // 4. FestivalDetail Entity 생성
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
                .posterFile(posterUrl)
                .contentFile(contentUrls)
                .entrpsnmH(detailReq.getEntrpsnmH())
                .runningTime(detailReq.getRunningTime())
                .updatedate( (detailReq.getUpdatedate()!=null && !detailReq.getUpdatedate().isBlank())
                        ? LocalDateTime.parse(detailReq.getUpdatedate().substring(0,19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        : LocalDateTime.now())
                .build();

        // 5. FestivalSchedule Entity 생성 및 연관 관계 설정
        List<FestivalSchedule> schedules = (request.getSchedules()==null ? List.of()
                : request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail)
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek().toUpperCase()))
                        .time(s.getTime())
                        .build())
                .collect(Collectors.toList()));
        detail.setSchedules(schedules);

        // 6. Festival Entity 생성 및 연관 관계 설정
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

        // 7. 저장
        detailRepository.saveAndFlush(detail);

        // 8. 저장 후 재조회 및 Kafka 전송
        FestivalDetail persisted = detailRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "저장된 공연 정보를 찾을 수 없습니다."));

        kafkaProducer.send(persisted, "FESTIVAL_CREATED");

        return FestivalRegisterResponseDTO.fromEntity(festival, persisted, schedules);
    }

    @Transactional
    public FestivalRegisterResponseDTO updateFestival(String fid, FestivalRegisterDTO request, Long userId, MultipartFile posterFile, List<MultipartFile> contentFiles) {
        // 1. 공연 존재 여부 및 소유자 확인
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        if (!festival.getFestivalDetail().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_OWNER);
        }

        // 2. DTO 유효성 검사
        var detailReq = request.getDetail();
        if (detailReq == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "공연 상세 정보가 누락되었습니다.");
        }

        // 3. 파일 업로드 (기존 파일 유지 로직)
        String posterUrl = (posterFile != null && !posterFile.isEmpty()) ? fileUploadService.uploadFile(posterFile) : festival.getPosterFile();
        List<String> contentUrls = new ArrayList<>();
        if (contentFiles != null && !contentFiles.isEmpty()) {
            for (MultipartFile file : contentFiles) {
                contentUrls.add(fileUploadService.uploadFile(file));
            }
        }

        LocalDate fdfrom = DateUtil.parseDate(request.getFdfrom());
        LocalDate fdto   = DateUtil.parseDate(request.getFdto());
        String computedState = calcState(fdfrom, fdto);

        // 4. Entity 업데이트
        FestivalDetail detail = festival.getFestivalDetail();
        detail.setFname(request.getFname());
        detail.setFdfrom(fdfrom);
        detail.setFdto(fdto);
        detail.setFcltynm(request.getFcltynm());
        detail.setPosterFile(posterUrl);
        detail.setContentFile(contentUrls);
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
                        ? LocalDateTime.parse(
                        detailReq.getUpdatedate().substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                        : LocalDateTime.now()
        );

        // 5. 스케줄 업데이트
        // 기존 스케줄 삭제 후 새로 저장 (⚠️ FestivalScheduleRepository에 deleteByFestivalDetail_Id(String fid) 메서드가 필요)
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
        festival.setPosterFile(posterUrl);
        festival.setFcltynm(request.getFcltynm());
        festival.setGenrenm(request.getGenrenm());
        festival.setPrfage(detailReq.getPrfage());
        festival.setFstate(computedState);

        festivalRepository.saveAndFlush(festival);
        Hibernate.initialize(festival.getFestivalDetail().getSchedules());

        kafkaProducer.send(festival.getFestivalDetail(), "FESTIVAL_UPDATED");

        return FestivalRegisterResponseDTO.fromEntity(festival, detail, schedules);
    }

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

    public List<FestivalRegisterResponseDTO> getFestivalsByRole(Long userId, boolean isAdmin) {
        List<Festival> festivals;
        if (isAdmin) {
            festivals = festivalRepository.findAll();
        } else {
            festivals = festivalRepository.findByFestivalDetail_UserId(userId);
        }

        return festivals.stream()
                .map(festival -> {
                    Hibernate.initialize(festival.getFestivalDetail().getSchedules());
                    return FestivalRegisterResponseDTO.fromEntity(festival, festival.getFestivalDetail(), festival.getFestivalDetail().getSchedules());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public FestivalRegisterResponseDTO getFestivalDetail(String fid, Long userId, boolean admin) {
        // 기존의 IllegalArgumentException, SecurityException 대신 BusinessException 사용
        FestivalDetail detail = detailRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND, "존재하지 않는 공연입니다."));

        if (!admin && !detail.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_OWNER);
        }

        Hibernate.initialize(detail.getSchedules());
        return FestivalRegisterResponseDTO.fromEntity(detail.getFestival(), detail, detail.getSchedules());
    }

    private String generateUniqueFid() {
        String fid;
        do {
            fid = "PF" + randomNumeric(6);
            if (detailRepository.existsById(fid)) {
                // 중복될 경우 BusinessException을 던져서 명확하게 알림
                throw new BusinessException(ErrorCode.DUPLICATE_FID, "공연 식별자 생성 중 중복이 발생했습니다.");
            }
        } while (detailRepository.existsById(fid)); // 이 부분은 이제 위에서 예외를 던지므로 사실상 의미가 없어짐
        return fid;
    }

    private String randomNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append((int) (Math.random() * 10));
        }
        return builder.toString();
    }

    private String calcState(LocalDate fdfrom, LocalDate fdto) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        if (fdfrom == null || fdto == null) return "공연예정";
        if (today.isBefore(fdfrom)) return "공연예정";
        if (today.isAfter(fdto)) return "공연완료";
        return "공연중";
    }
}