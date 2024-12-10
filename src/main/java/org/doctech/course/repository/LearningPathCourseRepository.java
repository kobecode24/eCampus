package org.doctech.course.repository;

import org.doctech.course.model.LearningPathCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LearningPathCourseRepository extends JpaRepository<LearningPathCourse, UUID> {

    List<LearningPathCourse> findByLearningPathIdOrderBySequenceOrder(UUID learningPathId);

    boolean existsByLearningPathIdAndCourseId(UUID learningPathId, UUID courseId);

    void deleteByLearningPathIdAndCourseId(UUID learningPathId, UUID courseId);

    @Query("SELECT MAX(lpc.sequenceOrder) FROM LearningPathCourse lpc WHERE lpc.learningPath.id = :learningPathId")
    Integer findMaxSequenceOrderByLearningPathId(UUID learningPathId);
}