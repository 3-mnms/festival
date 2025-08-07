package com.teckit.festival.mapper;

import com.teckit.festival.dto.request.FestivalScheduleDTO;
import com.teckit.festival.entity.FestivalSchedule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FestivalScheduleMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "festivalDetail", ignore = true),
            @Mapping(target = "time", source = "time")
    })
    FestivalSchedule toEntity(FestivalScheduleDTO dto);

    @Mappings({
            @Mapping(target = "festivalDetailId", expression = "java(mapFestivalDetailId(entity))"),
            @Mapping(target = "dayOfWeek", source = "dayOfWeek"),
            @Mapping(target = "time", source = "time")
    })
    FestivalScheduleDTO toDto(FestivalSchedule entity);

    List<FestivalScheduleDTO> toDtoList(List<FestivalSchedule> entityList);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "festivalDetail", ignore = true) // 명시적 무시
    void updateEntityFromDto(FestivalScheduleDTO dto, @MappingTarget FestivalSchedule entity);

    default String mapFestivalDetailId(FestivalSchedule entity) {
        return entity.getFestivalDetail() != null ? entity.getFestivalDetail().getId() : null;
    }
}
