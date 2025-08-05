package com.teckit.festival.service;

import com.teckit.festival.dto.response.FestivalDetailDTO;
import com.teckit.festival.dto.response.FestivalDetailListDTO;
import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.dto.response.FestivalListDTO;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalSchedule;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.FestivalRepository;
import com.teckit.festival.repository.FestivalScheduleRepository;
import com.teckit.festival.util.FestivalScheduleGenerator;
import com.teckit.festival.mapper.FestivalMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static com.teckit.festival.util.XmlApiUtil.fetchAndParseXml;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository festivalDetailRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;
    private final FestivalMapper festivalMapper;
    private final RestClient restClient;

    @Value("${festival-api-key}")
    private String festivalApiKey;

    @Transactional
    public Festival createFestivalWithDetail(FestivalDTO dto) {
        Festival festival = festivalMapper.toEntity(dto);
        return festivalRepository.save(festival);
    }

    @Transactional
    public Festival updateFestival(String fid, FestivalDTO dto) {
        Festival existingFestival = festivalRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        festivalMapper.updateEntityFromDto(dto, existingFestival);
        return festivalRepository.save(existingFestival);
    }

    @Transactional
    public void deleteFestivalByHost(String fid, Long hostId) {
        Festival festival = festivalRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        if (!festival.getHid().equals(hostId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_HOST);
        }
        festivalRepository.delete(festival);
    }

    @Transactional
    public void adminDeleteFestival(String fid) {
        Festival festival = festivalRepository.findById(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        festivalRepository.delete(festival);
    }

    public List<Festival> getFestivalsByHost(Long hostId) {
        return festivalRepository.findByHid(hostId);
    }

    public List<Festival> getAllFestivals() {
        return festivalRepository.findAll();
    }

    public Optional<FestivalDetail> getFestivalDetail(String fid) {
        return festivalDetailRepository.findByFestivalId(fid);
    }

    public List<String> getCategories() {
        return festivalDetailRepository.findDistinctGenrenm();
    }

    public Page<Festival> getFestivals(Pageable pageable) {
        return festivalRepository.findAll(pageable);
    }

    public List<Festival> searchByKeyword(String keyword) {
        return festivalRepository.findByFnameContaining(keyword);
    }

    public List<Festival> searchByGenre(String genre) {
        return festivalRepository.findByGenrename(genre);
    }

    public List<Festival> searchByGenreAndKeyword(String genre, String keyword) {
        return festivalRepository.findByGenrenameAndFnameContaining(genre, keyword);
    }

    @Transactional
    public void changeFstate() {
        List<Festival> festivals = festivalRepository.findAll();
        LocalDate today = LocalDate.now();
        for (Festival festival : festivals) {
            String newState = computeFstate(today, festival);
            if (!newState.equals(festival.getFstate())) {
                festival.setFstate(newState);
            }
        }
    }

    private String computeFstate(LocalDate today, Festival festival) {
        if (today.isBefore(festival.getFdfrom())) return "공연예정";
        if (!today.isAfter(festival.getFdto())) return "공연중";
        return "공연완료";
    }

    @Transactional
    public void increaseViews(String fid) {
        FestivalDetail festivalDetail = getDetail(fid);
        festivalDetail.setViews(festivalDetail.getViews() + 1);
    }

    public int getViews(String fid) {
        FestivalDetail detail = festivalDetailRepository.findByFestivalId(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        return detail.getViews();
    }

    private FestivalDetail getDetail(String fid) {
        return festivalDetailRepository.findByFestivalId(fid)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
    }

    @Transactional
    public void fetchAndSaveFestivalListAndDetail(String stdate, String eddate) {
        List<FestivalDTO> dtoList = fetchFestival(stdate, eddate);
        for (FestivalDTO festivalDTO : dtoList) {
            boolean exists = festivalRepository.existsById(festivalDTO.getMt20id());
            if (exists) continue;

            FestivalDetailDTO detailResponse = fetchFestivalDetail(festivalDTO.getMt20id());
            if (detailResponse == null) continue;

            Festival festival = festivalDTO.toEntity();
            int ticketPrice = FestivalScheduleGenerator.generateRandomPrice();
            int availableNop = FestivalScheduleGenerator.generateRandomAvailableNop();

            FestivalDetail festivalDetail = detailResponse.toEntity(festival, ticketPrice, availableNop);
            List<FestivalSchedule> festivalSchedules = FestivalScheduleGenerator.generateRandomSchedules(festivalDetail);
            festivalDetail.setSchedules(festivalSchedules);

            festival.setFestivalDetail(festivalDetail);  // 양방향 매핑 연결

            festivalRepository.save(festival);
            festivalDetailRepository.save(festivalDetail);
        }
    }

    public List<FestivalDTO> fetchFestival(String stdate, String eddate) {
        String uri = UriComponentsBuilder.fromPath("/")
                .queryParam("service", festivalApiKey)
                .queryParam("stdate", stdate)
                .queryParam("eddate", eddate)
                .queryParam("cpage", "1")
                .queryParam("rows", "100")
                .queryParam("afterdate", "20250601")
                .build()
                .toUriString();
        FestivalListDTO response = fetchAndParseXml(restClient, uri, FestivalListDTO.class);
        return response.getFestivalList();
    }

    public FestivalDetailDTO fetchFestivalDetail(String fid) {
        String uri = UriComponentsBuilder.fromPath("/" + fid)
                .queryParam("service", festivalApiKey)
                .build()
                .toUriString();
        FestivalDetailListDTO response = fetchAndParseXml(restClient, uri, FestivalDetailListDTO.class);
        List<FestivalDetailDTO> list = response.getFestivalDetailList();
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    @RequiredArgsConstructor
    @Service
    public class FestivalService {

        private final FestivalRepository festivalRepository;
        private final FestivalProducer festivalProducer;  // 추가

        @Transactional
        public Festival createFestivalWithDetail(FestivalDTO dto) {
            Festival festival = festivalMapper.toEntity(dto);
            Festival saved = festivalRepository.save(festival);

            // Kafka 메시지 보내기
            festivalProducer.send("festival-topic", "New Festival Created: " + saved.getFname());

            return saved;
        }
    }
}
