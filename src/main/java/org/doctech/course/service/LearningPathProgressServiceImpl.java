package org.doctech.course.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.LearningPathNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.course.dto.LearningPathProgressDTO;
import org.doctech.course.mapper.LearningPathProgressMapper;
import org.doctech.course.model.LearningPath;
import org.doctech.course.model.LearningPathProgress;
import org.doctech.course.repository.LearningPathProgressRepository;
import org.doctech.course.repository.LearningPathRepository;
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
public class LearningPathProgressServiceImpl implements LearningPathProgressService {

    private final LearningPathProgressRepository learningPathProgressRepository;
    private final LearningPathRepository learningPathRepository;
    private final UserRepository userRepository;
    private final UserProgressService userProgressService;
    private final LearningPathProgressMapper learningPathProgressMapper;

    @Override
    public LearningPathProgressDTO startLearningPath(UUID userId, UUID learningPathId) {
        validateUserAndLearningPath(userId, learningPathId);

        LearningPath learningPath = learningPathRepository.getReferenceById(learningPathId);

        LearningPathProgress existingProgress = learningPathProgressRepository
                .findByUserIdAndLearningPathId(userId, learningPathId)
                .orElseGet(() -> createInitialProgress(userId, learningPath));

        return learningPathProgressMapper.toDTO(existingProgress);
    }

    @Override
    public LearningPathProgressDTO updateProgress(UUID userId, UUID learningPathId, Integer completedCourses) {
        LearningPathProgress progress = learningPathProgressRepository
                .findByUserIdAndLearningPathId(userId, learningPathId)
                .orElseThrow(() -> new IllegalStateException("Learning path progress not found"));

        boolean wasCompleted = progress.isCompleted();
        progress.setCompletedCoursesCount(completedCourses);
        progress.setCompletionPercentage((completedCourses * 100) / progress.getTotalCoursesCount());

        if (!wasCompleted && progress.isCompleted()) {
            userProgressService.incrementCompletedLearningPath(userId);
        }

        return learningPathProgressMapper.toDTO(learningPathProgressRepository.save(progress));
    }

    @Override
    @Transactional(readOnly = true)
    public LearningPathProgressDTO getLearningPathProgress(UUID userId, UUID learningPathId) {
        return learningPathProgressRepository
                .findByUserIdAndLearningPathId(userId, learningPathId)
                .map(learningPathProgressMapper::toDTO)
                .orElseThrow(() -> new IllegalStateException("Learning path progress not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathProgressDTO> getUserCompletedPaths(UUID userId) {
        return learningPathProgressRepository
                .findByUserIdAndCompleted(userId, true)
                .stream()
                .map(learningPathProgressMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathProgressDTO> getUserInProgressPaths(UUID userId) {
        return learningPathProgressRepository
                .findByUserIdAndCompleted(userId, false)
                .stream()
                .map(learningPathProgressMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LearningPathProgressDTO> getPathParticipants(UUID learningPathId, Pageable pageable) {
        return learningPathProgressRepository
                .findByLearningPathId(learningPathId, pageable)
                .map(learningPathProgressMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearningPathProgressDTO> getRecentPaths(UUID userId, int limit) {
        return learningPathProgressRepository
                .findRecentlyAccessedByUser(userId, PageRequest.of(0, limit))
                .stream()
                .map(learningPathProgressMapper::toDTO)
                .toList();
    }

    private void validateUserAndLearningPath(UUID userId, UUID learningPathId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        if (!learningPathRepository.existsById(learningPathId)) {
            throw new LearningPathNotFoundException("Learning path not found with id: " + learningPathId);
        }
    }

    private LearningPathProgress createInitialProgress(UUID userId, LearningPath learningPath) {
        LearningPathProgress progress = LearningPathProgress.builder()
                .user(userRepository.getReferenceById(userId))
                .learningPath(learningPath)
                .completionPercentage(0)
                .completedCoursesCount(0)
                .totalCoursesCount(learningPath.getLearningPathCourses().size())
                .completed(false)
                .build();

        return learningPathProgressRepository.save(progress);
    }
}