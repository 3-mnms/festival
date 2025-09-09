package com.teckit.festival.service;

import com.teckit.festival.config.UserApiConfig;
import com.teckit.festival.dto.NearbyFestivalInterface;
import com.teckit.festival.dto.response.NearbyFestivalDTO;
import com.teckit.festival.dto.response.NearbyFestivalListDTO;
import com.teckit.festival.dto.response.UserGeocodeInfoDTO;
import com.teckit.festival.entity.NearbyFestival;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.repository.FestivalDetailRepository;
import com.teckit.festival.repository.NearbyFestivalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGeocodeService {
    private final UserApiConfig userApiConfig;
    private final FestivalDetailRepository festivalDetailRepository;
    private final NearbyFestivalRepository nearbyFestivalRepository;

    public UserGeocodeInfoDTO geoCodeInfo() {
        return userApiConfig.geocodeInfo();
    }

    @Transactional
    public NearbyFestivalListDTO getNearbyFestival(Long userId){
        UserGeocodeInfoDTO userGeocodeInfoDTO = geoCodeInfo();

        if(userGeocodeInfoDTO.getLatitude() == null || userGeocodeInfoDTO.getLongitude() == null)
            throw new BusinessException(ErrorCode.USER_GEOCODE_FAIL);

        List<NearbyFestival> nearbyList = nearbyFestivalRepository.findByUserIdOrderByDistanceAsc(userId);
        if(nearbyList.isEmpty()){
            return findNewNearby(userGeocodeInfoDTO);
        }
        else{
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            LocalDateTime updatedAt = nearbyList.get(0).getUpdatedAt();

            if (updatedAt.isBefore(threeDaysAgo)) {
                nearbyFestivalRepository.deleteByUserId(userId);
                return findNewNearby(userGeocodeInfoDTO);
            }
            else{
                List<NearbyFestivalDTO> nearbyFestivalDTOS = nearbyList.stream()
                        .map(NearbyFestivalDTO::fromEntity)
                        .toList();

                NearbyFestivalListDTO nearbyFestivalListDTO = new NearbyFestivalListDTO().builder().build();
                nearbyFestivalListDTO.setUserGeocodeInfo(userGeocodeInfoDTO);
                nearbyFestivalListDTO.setFestivalList(nearbyFestivalDTOS);

                return nearbyFestivalListDTO;
            }
        }
    }

    @Transactional
    public NearbyFestivalListDTO findNewNearby(UserGeocodeInfoDTO userGeocodeInfoDTO){

        NearbyFestivalListDTO nearbyFestivalListDTO = new NearbyFestivalListDTO().builder().build();
        nearbyFestivalListDTO.setUserGeocodeInfo(userGeocodeInfoDTO);

        List<NearbyFestivalDTO> nearbyFestivalDTOS = festivalDetailRepository.findTop3NearByFestivalDetail(userGeocodeInfoDTO.getLatitude(), userGeocodeInfoDTO.getLongitude(), 40)
                .stream()
                .map(NearbyFestivalInterface::toDto)
                .toList();

        Long userId = userGeocodeInfoDTO.getUserId();

        List<NearbyFestival> nearbyFestivals = nearbyFestivalDTOS.stream()
                .map(dto -> dto.toEntity(userId,
                        festivalDetailRepository.findByFestivalId(dto.getFestivalDetailId())
                                .orElseThrow(()->new BusinessException(ErrorCode.FESTIVAL_DETAIL_NOT_FOUND))
                ))
                .toList();

        nearbyFestivalRepository.saveAll(nearbyFestivals);
        nearbyFestivalListDTO.setFestivalList(nearbyFestivalDTOS);

        return nearbyFestivalListDTO;
    }

}
