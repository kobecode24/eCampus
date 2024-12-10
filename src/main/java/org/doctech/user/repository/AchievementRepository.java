package org.doctech.user.repository;

import org.doctech.user.model.Achievement;
import org.doctech.user.model.AchievementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    List<Achievement> findByType(AchievementType type);
    Page<Achievement> findByType(AchievementType type, Pageable pageable);
    boolean existsByTitle(String title);
}
