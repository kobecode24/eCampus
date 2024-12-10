package org.doctech.course.mapper;

import org.doctech.course.dto.LearningPathProgressDTO;
import org.doctech.course.model.LearningPathProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearningPathProgressMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "learningPath.id", target = "learningPathId")
    @Mapping(source = "learningPath.title", target = "learningPathTitle")
    LearningPathProgressDTO toDTO(LearningPathProgress learningPathProgress);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "learningPath", ignore = true)
    LearningPathProgress toEntity(LearningPathProgressDTO dto);
}
