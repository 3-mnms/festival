package com.teckit.festival.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "카카오 키워드 검색 전체 응답", name = "KakaoKeywordSearchDTO")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoKeywordSearchDTO {
    private List<KakaoResponseDTO> documents;
}