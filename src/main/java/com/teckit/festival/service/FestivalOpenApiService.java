// 📂 com.teckit.festival.service.FestivalOpenApiService
package com.teckit.festival.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class FestivalOpenApiService {

    private final RestClient restClient;

    // ✅ 환경변수 키를 예전 방식으로 통일
    @Value("${festival-api-key}")
    private String festivalApiKey;

    /**
     * 공연 목록 조회
     */
    public String getFestivalList(String stdate, String eddate) {
        String uri = UriComponentsBuilder.fromPath("")
                .queryParam("service", festivalApiKey) // ✅ API 문서에 맞춘 파라미터명
                .queryParam("stdate", stdate)
                .queryParam("eddate", eddate)
                .queryParam("cpage", "1")
                .queryParam("rows", "100")
                .queryParam("afterdate", "20250601")
                .build()
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }

    /**
     * 공연 상세 조회
     */
    public String getFestivalDetail(String id) {
        String uri = UriComponentsBuilder.fromPath("/" + id)
                .queryParam("service", festivalApiKey) // ✅ 동일하게 맞춤
                .build()
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }
}
