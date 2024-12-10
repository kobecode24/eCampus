package org.doctech.course.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.CourseNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.course.dto.CourseDTO;
import org.doctech.course.mapper.CourseMapper;
import org.doctech.course.model.Course;
import org.doctech.course.model.CourseStatus;
import org.doctech.course.repository.CourseRepository;
import org.doctech.user.model.User;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseDTO createCourse(CourseDTO courseDTO) {
        ValidationUtils.validate(courseDTO);

        User instructor = userRepository.findById(courseDTO.getInstructorId())
                .orElseThrow(() -> new UserNotFoundException("Instructor not found with id: " + courseDTO.getInstructorId()));

        Course course = courseMapper.toEntity(courseDTO);
        course.setInstructor(instructor);
        course.setStatus(CourseStatus.DRAFT);

        Course savedCourse = courseRepository.save(course);
        return courseMapper.toDTO(savedCourse);
    }

    @Override
    public CourseDTO updateCourse(UUID id, CourseDTO courseDTO) {
        ValidationUtils.validate(courseDTO);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));

        // Update only allowed fields
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setPointsToEarn(courseDTO.getPointsToEarn());
        course.setPointsCost(courseDTO.getPointsCost());

        Course updatedCourse = courseRepository.save(course);
        return courseMapper.toDTO(updatedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));
        return courseMapper.toDTO(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(courseMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseDTO> getCoursesByStatus(CourseStatus status, Pageable pageable) {
        return courseRepository.findByStatus(status, pageable)
                .map(courseMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByInstructor(UUID instructorId) {
        if (!userRepository.existsById(instructorId)) {
            throw new UserNotFoundException("Instructor not found with id: " + instructorId);
        }

        return courseRepository.findByInstructorId(instructorId).stream()
                .map(courseMapper::toDTO)
                .toList();
    }

    @Override
    public void deleteCourse(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new CourseNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Override
    public CourseDTO publishCourse(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));

        if (course.getStatus() != CourseStatus.DRAFT) {
            throw new IllegalStateException("Only courses in DRAFT status can be published");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        Course updatedCourse = courseRepository.save(course);
        return courseMapper.toDTO(updatedCourse);
    }

    @Override
    public CourseDTO archiveCourse(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + id));

        if (course.getStatus() == CourseStatus.ARCHIVED) {
            throw new IllegalStateException("Course is already archived");
        }

        course.setStatus(CourseStatus.ARCHIVED);
        Course updatedCourse = courseRepository.save(course);
        return courseMapper.toDTO(updatedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAvailableCourses(Integer availablePoints) {
        ValidationUtils.validateNotNull(availablePoints, "Available points cannot be null");

        return courseRepository.findAvailableCourses(availablePoints).stream()
                .map(courseMapper::toDTO)
                .toList();
    }
}
