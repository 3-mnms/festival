package com.teckit.festival.mapper;

import com.teckit.festival.dto.response.FestivalDTO;
import com.teckit.festival.entity.Festival;
import com.teckit.festival.entity.FestivalDetail;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FestivalMapper {

    @Mappings({
            @Mapping(target = "hid", source = "hid"),
            @Mapping(target = "genrename", source = "genrenm"),
            @Mapping(target = "fstate", source = "prfstate"),
            @Mapping(target = "festivalDetail", expression = "java(toFestivalDetail(dto))")
    })
    Festival toEntity(FestivalDTO dto);

    @Mappings({
            @Mapping(target = "hid", source = "hid"),
            @Mapping(target = "genrenm", source = "genrename")
    })
    FestivalDTO toDto(Festival entity);

    List<FestivalDTO> toDtoList(List<Festival> entityList);

    default FestivalDetail toFestivalDetail(FestivalDTO dto) {
        if (dto == null) return null;

        return FestivalDetail.builder()
                .fcltyid(dto.getFcltyid())
                .fname(dto.getFname())
                .fdfrom(dto.getFdfrom() != null ? dto.getFdfrom().toString() : null)
                .fdto(dto.getFdto() != null ? dto.getFdto().toString() : null)
                .fcltynm(dto.getFcltynm())
                .fcast(dto.getFcast())
                .fage(dto.getFage())
                .ticketPrice(dto.getTicketPrice())
                .poster(dto.getPoster())
                .story(dto.getStory())
                .genrenm(dto.getGenrenm())
                .fstate("READY")
                .visit("0")
                .availableNop(0)
                .updatedate(java.time.LocalDateTime.now().toString())
                .views(0)
                .styurls(dto.getStyurls())
                .schedules(new java.util.ArrayList<>())
                .build();
    }

    @Mappings({
            @Mapping(target = "genrename", source = "genrenm"),
            @Mapping(target = "fstate", source = "prfstate"),
            @Mapping(target = "festivalDetail", expression = "java(toFestivalDetail(dto))")
    })
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(FestivalDTO dto, @MappingTarget Festival entity);
}
