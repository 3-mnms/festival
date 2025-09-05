package com.teckit.festival.config;

import com.teckit.festival.dto.response.UserGeocodeInfoDTO;
import com.teckit.festival.exception.BusinessException;
import com.teckit.festival.exception.ErrorCode;
import com.teckit.festival.exception.global.SuccessResponse;
import com.teckit.festival.security.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


@Configuration
@RequiredArgsConstructor
public class UserApiConfig {
    private final WebClient webClient;

    @Value("${base.service.url}${user.geocode.url}")
    private String userServiceUrl;

    private void sendAuthentication(HttpHeaders headers){
        Authentication authentication = null;
        if(SecurityContextHolder.getContext() != null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        if(authentication == null)
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);

        Object principal = authentication.getPrincipal();
        if (principal != null) {
            headers.set("X-User-Id", principal.toString());
        }

        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)   // "ROLE_USER" 또는 "USER"
                .findFirst()
                .orElse(null);

        if (role != null && !role.isBlank()) {
            headers.set("X-User-Role", role); // 단일 값만 넣기
        }

        Object details = authentication.getDetails();
        if (details instanceof AuthDetails ad && ad.getUserName() != null) {
            String encoded = java.util.Base64.getUrlEncoder()
                    .encodeToString(ad.getUserName().getBytes(StandardCharsets.UTF_8));
            headers.set("X-User-Name", encoded);
        }

    }
    public UserGeocodeInfoDTO geocodeInfo(){
        SuccessResponse<UserGeocodeInfoDTO> userGeocodeInfo =
                webClient.get()
                .uri(userServiceUrl)
                .headers(this::sendAuthentication)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                            return Mono.error(new BusinessException(ErrorCode.USER_GEOCODE_FAIL));
                })
                .bodyToMono(new ParameterizedTypeReference<SuccessResponse<UserGeocodeInfoDTO>>() {})
                .block();
        return userGeocodeInfo.getData();
    }
}
