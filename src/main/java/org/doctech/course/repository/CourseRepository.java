package org.doctech.course.repository;

import org.doctech.course.model.Course;
import org.doctech.course.model.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    List<Course> findByInstructorId(UUID instructorId);

    @Query("SELECT c FROM Course c WHERE c.pointsCost <= :availablePoints AND c.status = 'PUBLISHED'")
    List<Course> findAvailableCourses(Integer availablePoints);

    boolean existsByTitleAndStatus(String title, CourseStatus status);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId")
    long countByInstructor(UUID instructorId);
}
