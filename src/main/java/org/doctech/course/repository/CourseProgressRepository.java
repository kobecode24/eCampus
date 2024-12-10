package org.doctech.course.repository;

import org.doctech.course.model.CourseProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, UUID> {
    Optional<CourseProgress> findByUserIdAndCourseId(UUID userId, UUID courseId);

    List<CourseProgress> findByUserIdAndCompleted(UUID userId, boolean completed);

    Page<CourseProgress> findByCourseId(UUID courseId, Pageable pageable);

    @Query("SELECT cp FROM CourseProgress cp WHERE cp.user.id = :userId ORDER BY cp.lastAccessedAt DESC")
    List<CourseProgress> findRecentlyAccessedByUser(UUID userId, Pageable pageable);
}
