package com.teckit.festival.dto.request;

import com.teckit.festival.entity.FestivalDetail;
import com.teckit.festival.entity.FestivalReview;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "페스티벌 기대평 생성 요청 DTO", name = "FestivalReviewDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalReviewRequestDTO {

    @Schema(description = "페스티벌 기대평 내용")
    @NotBlank(message = "기대평은 필수 입력사항 입니다.")
    @Size(max = 512, message = "내용은 512자 이상 작성할 수 없습니다.")
    private String reviewContent;

    public FestivalReview toEntity(Long userId, FestivalReviewRequestDTO festivalReviewRequestDTO, FestivalDetail festivalDetail) {
        return FestivalReview.builder()
                .reviewContent(reviewContent)
                .userId(userId)
                .festivalDetail(festivalDetail)
                .build();
    }
}
