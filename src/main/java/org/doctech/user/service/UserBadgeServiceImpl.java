package org.doctech.user.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.BadgeNotFoundException;
import org.doctech.common.exception.InsufficientPointsException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.user.dto.BadgeDTO;
import org.doctech.user.mapper.BadgeMapper;
import org.doctech.user.model.Badge;
import org.doctech.user.model.User;
import org.doctech.user.repository.BadgeRepository;
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
public class UserBadgeServiceImpl implements UserBadgeService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserProgressService userProgressService;
    private final BadgeMapper badgeMapper;

    @Override
    public BadgeDTO awardBadge(UUID userId, UUID badgeId) {
        User user = getUserById(userId);
        Badge badge = getBadgeById(badgeId);

        if (user.getBadges().contains(badge)) {
            throw new IllegalStateException("User already has this badge");
        }

        user.addBadge(badge);
        userProgressService.incrementEarnedBadge(userId);
        User updatedUser = userRepository.save(user);

        return badgeMapper.toDTO(badge);
    }

    @Override
    public BadgeDTO purchaseBadge(UUID userId, UUID badgeId) {
        User user = getUserById(userId);
        Badge badge = getBadgeById(badgeId);

        if (user.getBadges().contains(badge)) {
            throw new IllegalStateException("User already has this badge");
        }

        if (badge.getPointsCost() > user.getPoints()) {
            throw new InsufficientPointsException("Insufficient points to purchase badge");
        }

        user.spendPoints(badge.getPointsCost());
        user.addBadge(badge);
        userProgressService.incrementEarnedBadge(userId);
        User updatedUser = userRepository.save(user);

        return badgeMapper.toDTO(badge);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeDTO> getUserBadges(UUID userId) {
        User user = getUserById(userId);
        return user.getBadges().stream()
                .map(badgeMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BadgeDTO> getUserBadges(UUID userId, Pageable pageable) {
        // Implementation depends on your pagination requirements
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeDTO> getRecentlyAwardedBadges(UUID userId, int limit) {
        // Implementation depends on your tracking requirements
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasBadge(UUID userId, UUID badgeId) {
        User user = getUserById(userId);
        return user.getBadges().stream()
                .anyMatch(badge -> badge.getId().equals(badgeId));
    }

    @Override
    public void revokeBadge(UUID userId, UUID badgeId) {
        User user = getUserById(userId);
        Badge badge = getBadgeById(badgeId);

        if (!user.getBadges().contains(badge)) {
            throw new IllegalStateException("User doesn't have this badge");
        }

        user.getBadges().remove(badge);
        userRepository.save(user);
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private Badge getBadgeById(UUID badgeId) {
        return badgeRepository.findById(badgeId)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with id: " + badgeId));
    }
}