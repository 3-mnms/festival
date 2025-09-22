package com.teckit.festival.service;

import com.teckit.festival.dto.response.KakaoKeywordSearchDTO;
import com.teckit.festival.dto.response.KakaoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoSearchService {
    @Value("${kakao.restapi-key}")
    private String restApiKey;

    @Value("${kakao.search-base-url}")
    private String baseUrl;

    private final WebClient webClient = WebClient.builder().build();

    public Optional<KakaoResponseDTO> geocodeKeyword(String keyword) {
        log.info("keyword:"+keyword);
        if (keyword == null || keyword.isBlank())
            return Optional.empty();

        KakaoKeywordSearchDTO response = webClient.get()
                .uri(baseUrl + "/v2/local/search/keyword.json?query={query}&size={size}", keyword, 1)
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .bodyToMono(KakaoKeywordSearchDTO.class)
                .block();

        log.info("kakao response={}", response);

        if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
            return Optional.empty();
        }

        KakaoResponseDTO kakaoResponseDTO = response.getDocuments().get(0);
        return Optional.of(kakaoResponseDTO);
    }

    public Optional<List<KakaoResponseDTO>> activitySearch(String groupCode, double longitude, double latitude, int radius, int size) {
        log.info("카카오 장소 탐색 - category={}, x={}, y={}, radius={} size={}", groupCode, longitude, latitude, radius, size);

        KakaoKeywordSearchDTO response = webClient.get()
                .uri(baseUrl + "/v2/local/search/category.json?category_group_code={groupCode}&x={x}&y={y}&radius={radius}&size={size}"
                        ,groupCode, longitude, latitude, radius, size)
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .bodyToMono(KakaoKeywordSearchDTO.class)
                .block();

        log.info("kakao response={}", response);

        if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(response.getDocuments());
    }
}
