// package com.teckit.festival.dto.response;

package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Festival;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalListResponse {
    private String fid;
    private String prfnm;
    private LocalDate prfpdfrom;
    private LocalDate prfpdto;
    private String poster;

    public static FestivalListResponse fromEntity(Festival festival) {
        return FestivalListResponse.builder()
                .fid(festival.getFestivalDetail() != null ? festival.getFestivalDetail().getId() : null)  // **
                .prfnm(festival.getFname())
                .prfpdfrom(festival.getFdfrom())
                .prfpdto(festival.getFdto())
                .poster(festival.getPosterFile())
                .build();
    }
}
