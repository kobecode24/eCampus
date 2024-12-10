package org.doctech.course.mapper;

import org.doctech.course.dto.LearningPathCourseDTO;
import org.doctech.course.model.LearningPathCourse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningPathCourseMapper {

    @Mapping(source = "learningPath.id", target = "learningPathId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.title", target = "courseTitle")
    LearningPathCourseDTO toDTO(LearningPathCourse learningPathCourse);

    @Mapping(target = "learningPath", ignore = true)
    @Mapping(target = "course", ignore = true)
    LearningPathCourse toEntity(LearningPathCourseDTO dto);
}