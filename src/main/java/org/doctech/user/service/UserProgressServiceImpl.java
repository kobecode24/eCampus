package org.doctech.user.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.course.dto.UserProgressDTO;
import org.doctech.user.mapper.UserProgressMapper;
import org.doctech.user.model.UserProgress;
import org.doctech.user.repository.UserProgressRepository;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProgressServiceImpl implements UserProgressService {

    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;
    private final UserProgressMapper userProgressMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProgressDTO getUserProgress(UUID userId) {
        UserProgress userProgress = userProgressRepository.findByUserId(userId)
                .orElseGet(() -> createInitialProgress(userId));
        return userProgressMapper.toDTO(userProgress);
    }

    @Override
    public UserProgressDTO updatePoints(UUID userId, int points) {
        UserProgress userProgress = getUserProgressEntity(userId);
        userProgress.setTotalPoints(userProgress.getTotalPoints() + points);
        return userProgressMapper.toDTO(userProgressRepository.save(userProgress));
    }

    @Override
    public UserProgressDTO updateLevel(UUID userId, int level) {
        UserProgress userProgress = getUserProgressEntity(userId);
        userProgress.setCurrentLevel(level);
        return userProgressMapper.toDTO(userProgressRepository.save(userProgress));
    }

    @Override
    public UserProgressDTO incrementCompletedCourse(UUID userId) {
        UserProgress userProgress = getUserProgressEntity(userId);
        userProgress.setCompletedCoursesCount(userProgress.getCompletedCoursesCount() + 1);
        return userProgressMapper.toDTO(userProgressRepository.save(userProgress));
    }

    @Override
    public UserProgressDTO incrementCompletedLearningPath(UUID userId) {
        UserProgress userProgress = getUserProgressEntity(userId);
        userProgress.setCompletedLearningPathsCount(userProgress.getCompletedLearningPathsCount() + 1);
        return userProgressMapper.toDTO(userProgressRepository.save(userProgress));
    }

    @Override
    public UserProgressDTO incrementEarnedBadge(UUID userId) {
        UserProgress userProgress = getUserProgressEntity(userId);
        userProgress.setEarnedBadgesCount(userProgress.getEarnedBadgesCount() + 1);
        return userProgressMapper.toDTO(userProgressRepository.save(userProgress));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProgressDTO> getTopUsers(Pageable pageable) {
        return userProgressRepository.findTopUsers(pageable)
                .map(userProgressMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProgressDTO> getUsersByLevel(Integer level, Pageable pageable) {
        return userProgressRepository.findByLevel(level, pageable)
                .map(userProgressMapper::toDTO);
    }

    private UserProgress createInitialProgress(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        UserProgress initialProgress = UserProgress.builder()
                .user(userRepository.getReferenceById(userId))
                .totalPoints(0)
                .currentLevel(1)
                .completedCoursesCount(0)
                .completedLearningPathsCount(0)
                .earnedBadgesCount(0)
                .build();

        return userProgressRepository.save(initialProgress);
    }

    private UserProgress getUserProgressEntity(UUID userId) {
        return userProgressRepository.findByUserId(userId)
                .orElseGet(() -> createInitialProgress(userId));
    }
}