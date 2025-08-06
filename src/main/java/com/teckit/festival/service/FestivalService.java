package com.teckit.festival.service;

import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.dto.response.FestivalDetailDTO;
import com.teckit.festival.dto.response.FestivalDetailListDTO;
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
    private final RestClient restClient;

    @Value("${festival-api-key}")
    private String festivalApiKey;

    public Page<Festival> getFestivals(Pageable pageable) {
        return festivalRepository.findAll(pageable);
    }

    public Optional<FestivalDetail> getFestivalDetail(String id) {
        return festivalDetailRepository.findByFestivalId(id);
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
            int availableNOP = FestivalScheduleGenerator.generateRandomAvailableNOP();

            FestivalDetail festivalDetail = detailResponse.toEntity(festival, ticketPrice, availableNOP);
            List<FestivalSchedule> festivalSchedules = FestivalScheduleGenerator.generateRandomSchedules(festivalDetail);
            festivalDetail.setSchedules(festivalSchedules);

            festivalRepository.save(festival);
            festivalDetailRepository.save(festivalDetail);
        }
    }

    private List<FestivalDTO> fetchFestival(String stdate, String eddate) {
        String uri = UriComponentsBuilder.fromPath("")
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

    private FestivalDetailDTO fetchFestivalDetail(String id) {
        String uri = UriComponentsBuilder.fromPath("/" + id)
                .queryParam("service", festivalApiKey)
                .build()
                .toUriString();

        FestivalDetailListDTO response = fetchAndParseXml(restClient, uri, FestivalDetailListDTO.class);
        return response.getFestivalDetailList().get(0);
    }

    public List<Festival> searchByGenreAndKeyword(String genre, String keyword) {
        return festivalRepository.findByGenrenmAndFnameContaining(genre, keyword);
    }

    public List<Festival> searchByGenre(String genre) {
        return festivalRepository.findByGenrenm(genre);
    }

    public List<Festival> searchByKeyword(String keyword) {
        return festivalRepository.findByFnameContaining(keyword);
    }

    public List<String> getCategories() {
        return festivalDetailRepository.findDistinctGenrenm();
    }

    public int getViews(String id) {
        FestivalDetail detail = festivalDetailRepository.findByFestivalId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        return detail.getViews();
    }

    @Transactional
    public int increaseViews(String id) {
        FestivalDetail detail = festivalDetailRepository.findByFestivalId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));
        detail.setViews(detail.getViews() + 1);
        return detail.getViews();
    }

}