package org.doctech.user.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.AchievementNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.user.dto.UserAchievementDTO;
import org.doctech.user.mapper.UserAchievementMapper;
import org.doctech.user.model.Achievement;
import org.doctech.user.model.UserAchievement;
import org.doctech.user.repository.AchievementRepository;
import org.doctech.user.repository.UserAchievementRepository;
import org.doctech.user.repository.UserRepository;
import org.doctech.user.service.UserAchievementService;
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
public class UserAchievementServiceImpl implements UserAchievementService {

    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final UserProgressService userProgressService;
    private final UserAchievementMapper userAchievementMapper;

    @Override
    public UserAchievementDTO startAchievementProgress(UUID userId, UUID achievementId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new AchievementNotFoundException("Achievement not found with id: " + achievementId));

        UserAchievement existingProgress = userAchievementRepository
                .findByUserIdAndAchievementId(userId, achievementId)
                .orElseGet(() -> createInitialProgress(userId, achievement));

        return userAchievementMapper.toDTO(existingProgress);
    }

    @Override
    public UserAchievementDTO updateProgress(UUID userId, UUID achievementId, Integer progress) {
        UserAchievement userAchievement = userAchievementRepository
                .findByUserIdAndAchievementId(userId, achievementId)
                .orElseThrow(() -> new IllegalStateException("Achievement progress not found"));

        boolean wasCompleted = userAchievement.isCompleted();
        userAchievement.setCurrentProgress(progress);

        if (!wasCompleted && userAchievement.isCompleted()) {
            userProgressService.updatePoints(userId, userAchievement.getAchievement().getPointsReward());
        }

        return userAchievementMapper.toDTO(userAchievementRepository.save(userAchievement));
    }

    @Override
    @Transactional(readOnly = true)
    public UserAchievementDTO getUserAchievementProgress(UUID userId, UUID achievementId) {
        return userAchievementRepository
                .findByUserIdAndAchievementId(userId, achievementId)
                .map(userAchievementMapper::toDTO)
                .orElseThrow(() -> new IllegalStateException("Achievement progress not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getUserCompletedAchievements(UUID userId) {
        return userAchievementRepository.findByUserIdAndCompleted(userId, true)
                .stream()
                .map(userAchievementMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getUserInProgressAchievements(UUID userId) {
        return userAchievementRepository.findByUserIdAndCompleted(userId, false)
                .stream()
                .map(userAchievementMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserAchievementDTO> getAchievementParticipants(UUID achievementId, Pageable pageable) {
        return userAchievementRepository.findByAchievementId(achievementId, pageable)
                .map(userAchievementMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getRecentAchievements(UUID userId, int limit) {
        return userAchievementRepository.findRecentlyCompletedByUser(userId, PageRequest.of(0, limit))
                .stream()
                .map(userAchievementMapper::toDTO)
                .toList();
    }

    private UserAchievement createInitialProgress(UUID userId, Achievement achievement) {
        UserAchievement userAchievement = UserAchievement.builder()
                .user(userRepository.getReferenceById(userId))
                .achievement(achievement)
                .currentProgress(0)
                .completed(false)
                .build();

        return userAchievementRepository.save(userAchievement);
    }
}