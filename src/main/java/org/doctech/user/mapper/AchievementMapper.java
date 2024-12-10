package org.doctech.user.mapper;

import org.doctech.user.dto.AchievementDTO;
import org.doctech.user.model.Achievement;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AchievementMapper {
    AchievementDTO toDTO(Achievement achievement);
    Achievement toEntity(AchievementDTO dto);
}
