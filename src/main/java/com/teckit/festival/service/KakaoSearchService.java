package com.teckit.festival.service;

import com.teckit.festival.dto.response.KakaoKeywordSearchDTO;
import com.teckit.festival.dto.response.KakaoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoSearchService {
    @Value("${kakao.restapi-key}")
    private String restApiKey;

    @Value("${kakao.search-uri}")
    private String searchUri;

    private final WebClient webClient = WebClient.builder().build();

    public Optional<KakaoResponseDTO> geocodeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank())
            return Optional.empty();

        KakaoKeywordSearchDTO resp = webClient.get()
                .uri(uri -> uri
                        .path(searchUri)
                        .queryParam("query", keyword)
                        .queryParam("size", 1)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + restApiKey)
                .retrieve()
                .bodyToMono(KakaoKeywordSearchDTO.class)
                .block();

        if (resp == null || resp.getDocuments() == null || resp.getDocuments().isEmpty()) {
            return Optional.empty();
        }

        KakaoResponseDTO kakaoResponseDTO = resp.getDocuments().get(0);
        return Optional.of(kakaoResponseDTO);
    }
}
