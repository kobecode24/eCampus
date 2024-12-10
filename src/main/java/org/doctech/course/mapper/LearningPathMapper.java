package org.doctech.course.mapper;

import org.doctech.course.dto.LearningPathDTO;
import org.doctech.course.model.LearningPath;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {LearningPathCourseMapper.class})
public interface LearningPathMapper {

    @Mapping(source = "learningPathCourses", target = "courses")
    LearningPathDTO toDTO(LearningPath learningPath);

    @Mapping(target = "learningPathCourses", ignore = true)
    LearningPath toEntity(LearningPathDTO dto);
}