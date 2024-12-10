package org.doctech.user.mapper;

import org.doctech.course.dto.UserProgressDTO;
import org.doctech.user.model.UserProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProgressMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    UserProgressDTO toDTO(UserProgress userProgress);

    @Mapping(target = "user", ignore = true)
    UserProgress toEntity(UserProgressDTO dto);
}
