package org.doctech.user.repository;

import org.doctech.user.model.Badge;
import org.doctech.user.model.BadgeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    Optional<Badge> findByName(String name);

    List<Badge> findByType(BadgeType type);

    @Query("SELECT b FROM Badge b WHERE b.pointsRequired <= :points")
    List<Badge> findAvailableBadges(Integer points);

    @Query("SELECT b FROM Badge b WHERE b.pointsCost <= :points")
    List<Badge> findPurchaseableBadges(Integer points);

    boolean existsByName(String name);
}