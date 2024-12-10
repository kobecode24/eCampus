package org.doctech.user.repository;

import org.doctech.user.model.UserAchievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUserId(UUID userId);

    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);

    List<UserAchievement> findByUserIdAndCompleted(UUID userId, boolean completed);

    Page<UserAchievement> findByAchievementId(UUID achievementId, Pageable pageable);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.user.id = :userId ORDER BY ua.completedAt DESC")
    List<UserAchievement> findRecentlyCompletedByUser(UUID userId, Pageable pageable);

    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);
}
