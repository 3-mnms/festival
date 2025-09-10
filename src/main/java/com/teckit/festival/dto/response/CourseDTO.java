package com.teckit.festival.dto.response;

import com.teckit.festival.entity.Course;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "코스 추천 DTO", name = "CourseDTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    @Schema(description = "코스1")
    private String course1;

    @Schema(description = "코스2")
    private String course2;

    @Schema(description = "코스3")
    private String course3;

    public static CourseDTO toDto(Course course) {
        return CourseDTO.builder()
                .course1(course.getCourse1())
                .course2(course.getCourse2())
                .course3(course.getCourse3())
                .build();
    }
}
