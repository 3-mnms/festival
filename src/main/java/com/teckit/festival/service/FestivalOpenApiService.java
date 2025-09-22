// 외부 API 가져오는 용도
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


    @Value("${festival-api-key}")
    private String festivalApiKey;

    /**
     * 공연 목록 조회
     */
    public String getFestivalList(String stdate, String eddate) {
        String uri = UriComponentsBuilder.fromPath("")
                .queryParam("service", festivalApiKey)
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
                .queryParam("service", festivalApiKey)
                .build()
                .toUriString();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }
}
