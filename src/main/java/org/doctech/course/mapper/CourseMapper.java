package org.doctech.course.mapper;

import org.doctech.course.dto.CourseDTO;
import org.doctech.course.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {

    @Mapping(source = "instructor.id", target = "instructorId")
    @Mapping(source = "instructor.username", target = "instructorUsername")
    CourseDTO toDTO(Course course);

    @Mapping(target = "instructor", ignore = true)
    Course toEntity(CourseDTO courseDTO);
}
