package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalListResponseDTO {
    private String fid;
    private String prfnm;
    private LocalDate prfpdfrom;
    private LocalDate prfpdto;
    private String poster;

    public static FestivalListResponseDTO fromEntity(Festival festival) {
        return FestivalListResponseDTO.builder()
                .fid(festival.getFestivalDetail() != null ? festival.getFestivalDetail().getId() : null)  // **
                .prfnm(festival.getFname())
                .prfpdfrom(festival.getFdfrom())
                .prfpdto(festival.getFdto())
                .poster(festival.getPosterFile())
                .build();
    }
}
