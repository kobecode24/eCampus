package org.doctech.course.service;

import org.doctech.course.dto.CourseProgressDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CourseProgressService {
    CourseProgressDTO startCourse(UUID userId, UUID courseId);
    CourseProgressDTO updateProgress(UUID userId, UUID courseId, Integer completionPercentage);
    CourseProgressDTO getCourseProgress(UUID userId, UUID courseId);
    List<CourseProgressDTO> getUserCompletedCourses(UUID userId);
    List<CourseProgressDTO> getUserInProgressCourses(UUID userId);
    Page<CourseProgressDTO> getCourseParticipants(UUID courseId, Pageable pageable);
    List<CourseProgressDTO> getRecentCourses(UUID userId, int limit);
}
