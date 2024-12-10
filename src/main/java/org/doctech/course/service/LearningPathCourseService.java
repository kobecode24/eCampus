package org.doctech.course.service;

import org.doctech.course.dto.LearningPathCourseDTO;
import java.util.List;
import java.util.UUID;

public interface LearningPathCourseService {
    LearningPathCourseDTO addCourseToPath(UUID learningPathId, UUID courseId);
    void removeCourseFromPath(UUID learningPathId, UUID courseId);
    List<LearningPathCourseDTO> getCoursesByLearningPathId(UUID learningPathId);
    void updateCourseSequence(UUID learningPathId, List<UUID> courseIds);
}
