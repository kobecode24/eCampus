package org.doctech.course.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.CourseNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.course.dto.CourseProgressDTO;
import org.doctech.course.mapper.CourseProgressMapper;
import org.doctech.course.model.CourseProgress;
import org.doctech.course.repository.CourseProgressRepository;
import org.doctech.course.repository.CourseRepository;
import org.doctech.user.repository.UserRepository;
import org.doctech.user.service.UserProgressService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseProgressServiceImpl implements CourseProgressService {

    private final CourseProgressRepository courseProgressRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserProgressService userProgressService;
    private final CourseProgressMapper courseProgressMapper;

    @Override
    public CourseProgressDTO startCourse(UUID userId, UUID courseId) {
        validateUserAndCourse(userId, courseId);

        CourseProgress existingProgress = courseProgressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> createInitialProgress(userId, courseId));

        return courseProgressMapper.toDTO(existingProgress);
    }

    @Override
    public CourseProgressDTO updateProgress(UUID userId, UUID courseId, Integer completionPercentage) {
        CourseProgress progress = courseProgressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new IllegalStateException("Course progress not found"));

        boolean wasCompleted = progress.isCompleted();
        progress.setCompletionPercentage(completionPercentage);

        if (!wasCompleted && progress.isCompleted()) {
            userProgressService.incrementCompletedCourse(userId);
        }

        return courseProgressMapper.toDTO(courseProgressRepository.save(progress));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressDTO getCourseProgress(UUID userId, UUID courseId) {
        return courseProgressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .map(courseProgressMapper::toDTO)
                .orElseThrow(() -> new IllegalStateException("Course progress not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseProgressDTO> getUserCompletedCourses(UUID userId) {
        return courseProgressRepository
                .findByUserIdAndCompleted(userId, true)
                .stream()
                .map(courseProgressMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseProgressDTO> getUserInProgressCourses(UUID userId) {
        return courseProgressRepository
                .findByUserIdAndCompleted(userId, false)
                .stream()
                .map(courseProgressMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseProgressDTO> getCourseParticipants(UUID courseId, Pageable pageable) {
        return courseProgressRepository
                .findByCourseId(courseId, pageable)
                .map(courseProgressMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseProgressDTO> getRecentCourses(UUID userId, int limit) {
        return courseProgressRepository
                .findRecentlyAccessedByUser(userId, PageRequest.of(0, limit))
                .stream()
                .map(courseProgressMapper::toDTO)
                .toList();
    }

    private void validateUserAndCourse(UUID userId, UUID courseId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException("Course not found with id: " + courseId);
        }
    }

    private CourseProgress createInitialProgress(UUID userId, UUID courseId) {
        CourseProgress progress = CourseProgress.builder()
                .user(userRepository.getReferenceById(userId))
                .course(courseRepository.getReferenceById(courseId))
                .completionPercentage(0)
                .completed(false)
                .build();

        return courseProgressRepository.save(progress);
    }
}