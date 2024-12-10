package org.doctech.course.service;

import org.doctech.course.dto.CourseDTO;
import org.doctech.course.model.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    CourseDTO createCourse(CourseDTO courseDTO);

    CourseDTO updateCourse(UUID id, CourseDTO courseDTO);

    CourseDTO getCourseById(UUID id);

    Page<CourseDTO> getAllCourses(Pageable pageable);

    Page<CourseDTO> getCoursesByStatus(CourseStatus status, Pageable pageable);

    List<CourseDTO> getCoursesByInstructor(UUID instructorId);

    void deleteCourse(UUID id);

    CourseDTO publishCourse(UUID id);

    CourseDTO archiveCourse(UUID id);

    List<CourseDTO> getAvailableCourses(Integer availablePoints);
}