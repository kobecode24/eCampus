package org.doctech.user.repository;

import org.doctech.user.model.UserProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {
    Optional<UserProgress> findByUserId(UUID userId);

    @Query("SELECT up FROM UserProgress up ORDER BY up.totalPoints DESC")
    Page<UserProgress> findTopUsers(Pageable pageable);

    @Query("SELECT up FROM UserProgress up WHERE up.currentLevel = :level")
    Page<UserProgress> findByLevel(Integer level, Pageable pageable);
}
