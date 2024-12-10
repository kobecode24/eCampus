package org.doctech.user.mapper;

import org.doctech.user.dto.UserAchievementDTO;
import org.doctech.user.model.UserAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAchievementMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "achievement.id", target = "achievementId")
    @Mapping(source = "achievement.title", target = "achievementTitle")
    @Mapping(source = "achievement.requiredProgress", target = "requiredProgress")
    UserAchievementDTO toDTO(UserAchievement userAchievement);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "achievement", ignore = true)
    UserAchievement toEntity(UserAchievementDTO dto);
}
