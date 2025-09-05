package com.teckit.festival.service;

import com.teckit.festival.config.UserApiConfig;
import com.teckit.festival.dto.response.UserGeocodeInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGeocodeService {
    private final UserApiConfig userApiConfig;

    public UserGeocodeInfoDTO geoCodeInfo() {
        return userApiConfig.geocodeInfo();
    }

}
