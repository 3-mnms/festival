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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final FestivalIdGenerator festivalIdGenerator;

    private final S3Client s3Client;   // 삭제용
    private final String s3BucketName; // 삭제용
    private final S3Service s3Service; // 업로드용 Presigned URL 서비스

    // 공연 등록 (파일 S3 업로드 후 URL DB 저장)
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

        // 1. S3 업로드
        String posterUrl = (posterFile != null) ? s3Service.uploadFile(posterFile) : null;
        List<String> contentUrls = (contentFiles != null && !contentFiles.isEmpty())
                ? contentFiles.stream().map(s3Service::uploadFile).toList()
                : List.of();

        // 2. 기본 데이터 가공
        String fid = festivalIdGenerator.generateUniqueFid();
        int safeTicketPick = Math.max(1, detailReq.getTicketPick());
        int safeMaxPurchase = Math.max(1, detailReq.getMaxPurchase());
        int safeAvailable = Math.max(0, detailReq.getAvailableNOP());

        LocalDate fdfrom = DateUtil.parseDate(request.getFdfrom());
        LocalDate fdto = DateUtil.parseDate(request.getFdto());
        String computedState = FestivalStatusUtil.calcState(fdfrom, fdto);

        // 3. FestivalDetail 생성
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

        // 4. Schedule 매핑
        List<FestivalSchedule> schedules = (request.getSchedules() == null ? List.of()
                : request.getSchedules().stream()
                .map(s -> FestivalSchedule.builder()
                        .festivalDetail(detail)
                        .dayOfWeek(FestivalScheduleDay.valueOf(s.getDayOfWeek().toUpperCase()))
                        .time(s.getTime())
                        .build())
                .toList());
        detail.setSchedules(schedules);

        // 5. Festival 생성
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

        // 6. 저장 및 Kafka 전송
        detailRepository.saveAndFlush(detail);
        kafkaProducer.send(detail, "FESTIVAL_CREATED");

        // 7. DTO 반환
        return FestivalRegisterResponseDTO.fromEntity(festival, detail, schedules);
    }

    // 공연 수정 (파일 교체 시 S3 업로드/삭제)
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

        FestivalDetail detail = festival.getFestivalDetail();

        // 포스터 교체
        if (posterFile != null) {
            if (detail.getPosterFile() != null) {
                deleteFileFromS3(detail.getPosterFile()); // 기존 삭제
            }
            String newPosterUrl = s3Service.uploadFile(posterFile); // 새 업로드
            detail.setPosterFile(newPosterUrl);
            festival.setPosterFile(newPosterUrl);
        }

        // 콘텐츠 교체
        if (contentFiles != null && !contentFiles.isEmpty()) {
            if (detail.getContentFile() != null) {
                detail.getContentFile().forEach(this::deleteFileFromS3);
            }
            List<String> newContentUrls = contentFiles.stream()
                    .map(s3Service::uploadFile)
                    .toList();
            detail.setContentFile(newContentUrls);
        }

        LocalDate fdfrom = DateUtil.parseDate(request.getFdfrom());
        LocalDate fdto = DateUtil.parseDate(request.getFdto());
        String computedState = FestivalStatusUtil.calcState(fdfrom, fdto);

        detail.setFname(request.getFname());
        detail.setFdfrom(fdfrom);
        detail.setFdto(fdto);
        detail.setFcltynm(request.getFcltynm());
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
        detail.setUpdatedate(LocalDateTime.now());

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
        festival.setPosterFile(detail.getPosterFile());
        festival.setFcltynm(request.getFcltynm());
        festival.setGenrenm(request.getGenrenm());
        festival.setPrfage(detailReq.getPrfage());
        festival.setFstate(computedState);

        festivalRepository.saveAndFlush(festival);
        Hibernate.initialize(festival.getFestivalDetail().getSchedules());

        kafkaProducer.send(festival.getFestivalDetail(), "FESTIVAL_UPDATED");

        return FestivalRegisterResponseDTO.fromEntity(festival, detail, schedules);
    }

    /**
     * 공연 삭제 (DB와 S3 파일 모두 삭제)
     */
    @Transactional
    public void deleteFestivalByHost(String fid, Long userId, boolean isAdmin) {
        Festival festival = festivalRepository.findByFestivalDetail_Id(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        if (!isAdmin && !festival.getFestivalDetail().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_OWNER);
        }

        FestivalDetail detail = festival.getFestivalDetail();

        // S3에서 포스터 및 콘텐츠 파일 삭제
        if (detail.getPosterFile() != null) {
            deleteFileFromS3(detail.getPosterFile());
        }
        if (detail.getContentFile() != null) {
            detail.getContentFile().forEach(this::deleteFileFromS3);
        }

        kafkaProducer.sendDeleted(fid);

        festivalRepository.delete(festival);
        detailRepository.deleteById(fid);
    }

    /**
     * S3 객체 삭제 헬퍼 메서드
     */
    private void deleteFileFromS3(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                return;
            }
            String key = extractS3KeyFromUrl(fileUrl);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3BucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 object deleted successfully: {}", fileUrl);
        } catch (Exception e) {
            log.error("Failed to delete S3 object: {}", fileUrl, e);
        }
    }

    /**
     * S3 URL에서 객체 키(Key)를 추출하는 헬퍼 메서드
     */
    private String extractS3KeyFromUrl(String fileUrl) {
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            return url.getPath().substring(1);
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + fileUrl, e);
        }
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