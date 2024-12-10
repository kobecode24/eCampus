package org.doctech.course.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.CourseNotFoundException;
import org.doctech.common.exception.LearningPathNotFoundException;
import org.doctech.course.dto.LearningPathCourseDTO;
import org.doctech.course.mapper.LearningPathCourseMapper;
import org.doctech.course.model.Course;
import org.doctech.course.model.LearningPath;
import org.doctech.course.model.LearningPathCourse;
import org.doctech.course.repository.CourseRepository;
import org.doctech.course.repository.LearningPathCourseRepository;
import org.doctech.course.repository.LearningPathRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningPathCourseServiceImpl implements LearningPathCourseService {

    private final LearningPathCourseRepository learningPathCourseRepository;
    private final LearningPathRepository learningPathRepository;
    private final CourseRepository courseRepository;
    private final LearningPathCourseMapper learningPathCourseMapper;

    @Override
    public LearningPathCourseDTO addCourseToPath(UUID learningPathId, UUID courseId) {
        LearningPath learningPath = learningPathRepository.findById(learningPathId)
                .orElseThrow(() -> new LearningPathNotFoundException("Learning path not found with id: " + learningPathId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        if (learningPathCourseRepository.existsByLearningPathIdAndCourseId(learningPathId, courseId)) {
            throw new IllegalStateException("Course is already in the learning path");
        }

        Integer nextOrder = learningPathCourseRepository.findMaxSequenceOrderByLearningPathId(learningPathId);
        nextOrder = (nextOrder == null) ? 1 : nextOrder + 1;

        LearningPathCourse learningPathCourse = LearningPathCourse.builder()
                .learningPath(learningPath)
                .course(course)
                .sequenceOrder(nextOrder)
                .build();

        LearningPathCourse savedLearningPathCourse = learningPathCourseRepository.save(learningPathCourse);
        return learningPathCourseMapper.toDTO(savedLearningPathCourse);
    }

    @Override
    public void removeCourseFromPath(UUID learningPathId, UUID courseId) {
        if (!learningPathCourseRepository.existsByLearningPathIdAndCourseId(learningPathId, courseId)) {
            throw new IllegalStateException("Course is not in the learning path");
        }
        learningPathCourseRepository.deleteByLearningPathIdAndCourseId(learningPathId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathCourseDTO> getCoursesByLearningPathId(UUID learningPathId) {
        return learningPathCourseRepository.findByLearningPathIdOrderBySequenceOrder(learningPathId).stream()
                .map(learningPathCourseMapper::toDTO)
                .toList();
    }

    @Override
    public void updateCourseSequence(UUID learningPathId, List<UUID> courseIds) {
        if (!learningPathRepository.existsById(learningPathId)) {
            throw new LearningPathNotFoundException("Learning path not found with id: " + learningPathId);
        }

        List<LearningPathCourse> existingCourses = learningPathCourseRepository
                .findByLearningPathIdOrderBySequenceOrder(learningPathId);

        // Verify all courses exist in the learning path
        if (existingCourses.size() != courseIds.size() ||
                !existingCourses.stream()
                        .map(lpc -> lpc.getCourse().getId())
                        .collect(java.util.stream.Collectors.toSet())
                        .containsAll(courseIds)) {
            throw new IllegalArgumentException("Invalid course sequence provided");
        }

        // Update sequence orders
        for (int i = 0; i < courseIds.size(); i++) {
            UUID courseId = courseIds.get(i);
            int finalI = i;
            existingCourses.stream()
                    .filter(lpc -> lpc.getCourse().getId().equals(courseId))
                    .findFirst()
                    .ifPresent(lpc -> lpc.setSequenceOrder(finalI + 1));
        }

        learningPathCourseRepository.saveAll(existingCourses);
    }
}