package org.doctech.user.service;

import org.doctech.user.dto.UserAchievementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserAchievementService {
    UserAchievementDTO startAchievementProgress(UUID userId, UUID achievementId);
    UserAchievementDTO updateProgress(UUID userId, UUID achievementId, Integer progress);
    UserAchievementDTO getUserAchievementProgress(UUID userId, UUID achievementId);
    List<UserAchievementDTO> getUserCompletedAchievements(UUID userId);
    List<UserAchievementDTO> getUserInProgressAchievements(UUID userId);
    Page<UserAchievementDTO> getAchievementParticipants(UUID achievementId, Pageable pageable);
    List<UserAchievementDTO> getRecentAchievements(UUID userId, int limit);
}
