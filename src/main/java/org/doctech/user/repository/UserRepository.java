package org.doctech.user.repository;

import org.doctech.user.model.User;
import org.doctech.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(String roleName);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countByRoleName(String roleName);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.badges WHERE u.id = :id")
    Optional<User> findByIdWithBadges(UUID id);

    @Query("SELECT u FROM User u ORDER BY u.points DESC LIMIT :limit")
    List<User> findTopUsersByPoints(int limit);

    long countByCredentialsNonExpired(boolean credentialsNonExpired);
    long countByCreatedAtAfter(LocalDateTime dateTime);
}