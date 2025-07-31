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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
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

    public List<String> getCategories(){
        return festivalDetailRepository.findDistinctGenrenm();
    }


    public Page<Festival> getFestivals(Pageable pageable) {
        return festivalRepository.findAll(pageable);
    }

    public Optional<FestivalDetail> getFestivalDetail(String id){
        return festivalDetailRepository.findByFestivalId(id);
    }

    public int getViews(String id){
        FestivalDetail festivalDetail = festivalDetailRepository.findByFestivalId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));


        return festivalDetail.getViews();

    }



    @Transactional
    public int increaseViews(String id) {
        FestivalDetail festivalDetail = festivalDetailRepository.findByFestivalId(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FESTIVAL_NOT_FOUND));

        festivalDetail.setViews(festivalDetail.getViews() + 1); // 조회수 증가

        // JPA @Transactional 이기 때문에 save() 호출 없이도 flush됨
        return festivalDetail.getViews();
    }


    @Transactional
    public void fetchAndSaveFestivalListAndDetail() {

        List<FestivalDTO> dtoList = fetchFestival();

//        todo : 불필요한 연산 과정 줄이기
        for (FestivalDTO festivalDTO : dtoList) {
            boolean exists = festivalRepository.existsByFnameAndFdfromAndFdto(
                    festivalDTO.getPrfnm(), festivalDTO.getPrfpdfrom(), festivalDTO.getPrfpdto());

            if(exists) continue;

            FestivalDetailDTO detailResponse=fetchFestivalDetail(festivalDTO.getMt20id());

            if(detailResponse==null) continue;

            Festival festival=festivalDTO.toEntity();

            int ticketPrice=FestivalScheduleGenerator.generateRandomPrice();
            int availableNOP=FestivalScheduleGenerator.generateRandomAvailableNOP();

            FestivalDetail festivalDetail=detailResponse.toEntity(festival,ticketPrice,availableNOP);
            List<FestivalSchedule> festivalSchedules= FestivalScheduleGenerator.generateRandomSchedules(festivalDetail);
            festivalDetail.setSchedules(festivalSchedules);

            festivalRepository.save(festival);
            festivalDetailRepository.save(festivalDetail);
        }
    }

    public List<FestivalDTO> fetchFestival(){
        String uri = UriComponentsBuilder.fromPath("")
                .queryParam("service", festivalApiKey)
                .queryParam("stdate", "20250701")
                .queryParam("eddate", "20250730")
                .queryParam("cpage", "1")
                .queryParam("rows", "100")
                .queryParam("afterdate", "20250601")
                .build()
                .toUriString();

        FestivalListDTO response = fetchAndParseXml(restClient,uri, FestivalListDTO.class);
        List<FestivalDTO> dtoList = response.getFestivalList();

        return dtoList;
    }

    public FestivalDetailDTO fetchFestivalDetail(String id){
        String uri = UriComponentsBuilder.fromPath("/" + id)
                .queryParam("service", festivalApiKey)
                .build()
                .toUriString();

        FestivalDetailListDTO response = fetchAndParseXml(restClient,uri, FestivalDetailListDTO.class);
        return response.getFestivalDetailList().get(0);
    }
}
