package org.doctech.user.service;

import org.doctech.user.dto.BadgeDTO;
import org.doctech.user.model.BadgeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BadgeService {
    BadgeDTO createBadge(BadgeDTO badgeDTO);
    BadgeDTO updateBadge(UUID id, BadgeDTO badgeDTO);
    BadgeDTO getBadgeById(UUID id);
    BadgeDTO getBadgeByName(String name);
    List<BadgeDTO> getBadgesByType(BadgeType type);
    Page<BadgeDTO> getAllBadges(Pageable pageable);
    List<BadgeDTO> getAvailableBadges(Integer points);
    List<BadgeDTO> getPurchaseableBadges(Integer points);
    void deleteBadge(UUID id);
}