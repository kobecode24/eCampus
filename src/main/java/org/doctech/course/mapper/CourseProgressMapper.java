package org.doctech.course.mapper;

import org.doctech.course.dto.CourseProgressDTO;
import org.doctech.course.model.CourseProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseProgressMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.title", target = "courseTitle")
    CourseProgressDTO toDTO(CourseProgress courseProgress);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    CourseProgress toEntity(CourseProgressDTO dto);
}
