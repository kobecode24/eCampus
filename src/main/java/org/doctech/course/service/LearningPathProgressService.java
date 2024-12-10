package org.doctech.course.service;

import org.doctech.course.dto.LearningPathProgressDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface LearningPathProgressService {
    LearningPathProgressDTO startLearningPath(UUID userId, UUID learningPathId);
    LearningPathProgressDTO updateProgress(UUID userId, UUID learningPathId, Integer completedCourses);
    LearningPathProgressDTO getLearningPathProgress(UUID userId, UUID learningPathId);
    List<LearningPathProgressDTO> getUserCompletedPaths(UUID userId);
    List<LearningPathProgressDTO> getUserInProgressPaths(UUID userId);
    Page<LearningPathProgressDTO> getPathParticipants(UUID learningPathId, Pageable pageable);
    List<LearningPathProgressDTO> getRecentPaths(UUID userId, int limit);
}
