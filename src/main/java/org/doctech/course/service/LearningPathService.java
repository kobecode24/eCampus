package org.doctech.course.service;

import org.doctech.course.dto.LearningPathDTO;
import org.doctech.course.model.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface LearningPathService {
    LearningPathDTO createLearningPath(LearningPathDTO learningPathDTO);
    LearningPathDTO updateLearningPath(UUID id, LearningPathDTO learningPathDTO);
    LearningPathDTO getLearningPathById(UUID id);
    Page<LearningPathDTO> getAllLearningPaths(Pageable pageable);
    Page<LearningPathDTO> getLearningPathsByDifficulty(DifficultyLevel difficulty, Pageable pageable);
    void deleteLearningPath(UUID id);
    List<LearningPathDTO> getAvailablePaths(Integer maxPoints);
}
