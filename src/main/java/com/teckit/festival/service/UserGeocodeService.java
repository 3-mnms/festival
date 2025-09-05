package com.teckit.festival.service;

import com.teckit.festival.config.UserApiConfig;
import com.teckit.festival.dto.NearbyFestivalInterface;
import com.teckit.festival.dto.response.NearbyFestivalDTO;
import com.teckit.festival.dto.response.NearbyFestivalListDTO;
import com.teckit.festival.dto.response.UserGeocodeInfoDTO;
import com.teckit.festival.repository.FestivalDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGeocodeService {
    private final UserApiConfig userApiConfig;
    private final FestivalDetailRepository festivalDetailRepository;

    public UserGeocodeInfoDTO geoCodeInfo() {
        return userApiConfig.geocodeInfo();
    }

    public NearbyFestivalListDTO findNearbyFestival(){
        UserGeocodeInfoDTO userGeocodeInfoDTO = geoCodeInfo();

        NearbyFestivalListDTO nearbyFestivalListDTO = new NearbyFestivalListDTO().builder().build();
        nearbyFestivalListDTO.setUserGeocodeInfo(userGeocodeInfoDTO);

        List<NearbyFestivalDTO> nearbyFestivalDTOS = festivalDetailRepository.findTop3NearByFestivalDetail(userGeocodeInfoDTO.getLatitude(), userGeocodeInfoDTO.getLongitude(), 40)
                                                            .stream()
                                                            .map(NearbyFestivalInterface::toDto)   // ← 여기 한 줄로 변환 끝
                                                            .toList();
        nearbyFestivalListDTO.setFestivalList(nearbyFestivalDTOS);

        return nearbyFestivalListDTO;
    }

}
