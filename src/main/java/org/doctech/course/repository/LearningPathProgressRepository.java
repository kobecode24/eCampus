package org.doctech.course.repository;

import org.doctech.course.model.LearningPathProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningPathProgressRepository extends JpaRepository<LearningPathProgress, UUID> {
    Optional<LearningPathProgress> findByUserIdAndLearningPathId(UUID userId, UUID learningPathId);

    List<LearningPathProgress> findByUserIdAndCompleted(UUID userId, boolean completed);

    Page<LearningPathProgress> findByLearningPathId(UUID learningPathId, Pageable pageable);

    @Query("SELECT lpp FROM LearningPathProgress lpp WHERE lpp.user.id = :userId ORDER BY lpp.lastAccessedAt DESC")
    List<LearningPathProgress> findRecentlyAccessedByUser(UUID userId, Pageable pageable);
}
