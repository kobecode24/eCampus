package org.doctech.user.service;

import org.doctech.course.dto.UserProgressDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserProgressService {
    UserProgressDTO getUserProgress(UUID userId);
    UserProgressDTO updatePoints(UUID userId, int points);
    UserProgressDTO updateLevel(UUID userId, int level);
    UserProgressDTO incrementCompletedCourse(UUID userId);
    UserProgressDTO incrementCompletedLearningPath(UUID userId);
    UserProgressDTO incrementEarnedBadge(UUID userId);
    Page<UserProgressDTO> getTopUsers(Pageable pageable);
    Page<UserProgressDTO> getUsersByLevel(Integer level, Pageable pageable);
}
