package com.teckit.festival.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 주소 위도 경도 정보 DTO", name = "UserGeocodeInfoDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGeocodeInfoDTO {
    @Schema(description = "사용자 userId")
    private Long userId;

    @Schema(description = "사용자 주소 위도")
    private Double latitude; //위도

    @Schema(description = "사용자 주소 경도")
    private Double longitude;//경도
}
