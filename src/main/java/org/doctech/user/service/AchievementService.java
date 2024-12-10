package org.doctech.user.service;

import org.doctech.user.dto.AchievementDTO;
import org.doctech.user.model.AchievementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AchievementService {
    AchievementDTO createAchievement(AchievementDTO achievementDTO);
    AchievementDTO updateAchievement(UUID id, AchievementDTO achievementDTO);
    AchievementDTO getAchievementById(UUID id);
    List<AchievementDTO> getAchievementsByType(AchievementType type);
    Page<AchievementDTO> getAllAchievements(Pageable pageable);
    void deleteAchievement(UUID id);
}
