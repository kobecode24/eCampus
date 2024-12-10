package org.doctech.user.service;

import org.doctech.user.dto.BadgeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserBadgeService {
    BadgeDTO awardBadge(UUID userId, UUID badgeId);
    BadgeDTO purchaseBadge(UUID userId, UUID badgeId);
    List<BadgeDTO> getUserBadges(UUID userId);
    Page<BadgeDTO> getUserBadges(UUID userId, Pageable pageable);
    List<BadgeDTO> getRecentlyAwardedBadges(UUID userId, int limit);
    boolean hasBadge(UUID userId, UUID badgeId);
    void revokeBadge(UUID userId, UUID badgeId);
}
