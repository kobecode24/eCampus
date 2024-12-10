package org.doctech.course.repository;

import org.doctech.course.model.DifficultyLevel;
import org.doctech.course.model.LearningPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, UUID> {

    Page<LearningPath> findByDifficulty(DifficultyLevel difficulty, Pageable pageable);

    @Query("SELECT lp FROM LearningPath lp WHERE lp.totalPoints <= :maxPoints")
    List<LearningPath> findAvailablePaths(Integer maxPoints);

    @Query("SELECT DISTINCT lp FROM LearningPath lp " +
            "LEFT JOIN FETCH lp.learningPathCourses lpc " +
            "LEFT JOIN FETCH lpc.course " +
            "WHERE lp.id = :id")
    Optional<LearningPath> findByIdWithCourses(UUID id);

    boolean existsByTitle(String title);
}