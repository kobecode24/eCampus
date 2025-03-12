package org.doctech.user.service;

import jakarta.validation.constraints.NotBlank;
import org.doctech.user.dto.UserDTO;
import org.doctech.user.dto.UserStatisticsDTO;
import org.doctech.user.model.Badge;
import org.doctech.user.model.User;
import org.doctech.user.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO registerUser(String email, String username, String password, UserRole roleType);

    UserDTO getUserById(UUID id);

    UserDTO getUserByEmail(String email);

    UserDTO getUserByUsername(String username);

    Page<UserDTO> getAllUsers(Pageable pageable);

    List<UserDTO> getUsersByRole(UserRole roleType);

    UserDTO updateUser(UUID id, UserDTO userDTO);

    void deleteUser(UUID id);

    UserDTO addPoints(UUID userId, int points);

    UserDTO spendPoints(UUID userId, int points);

    UserDTO addRole(UUID userId, String roleName);

    UserDTO removeRole(UUID userId, String roleName);

    UserDTO awardBadge(UUID userId, UUID badgeId);

    List<Badge> getUserBadges(UUID userId);

    void updateLastLogin(UUID userId);

    UserDTO toggleUserEnabled(UUID userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<UserDTO> getTopUsersByPoints(int limit);

    long countByRole(UserRole role);

    boolean isCurrentUser(UUID userId, Object principal);

    User updateUserAvatar(UUID userId, String url);

    void updatePassword(UUID id, @NotBlank(message = "Current password is required") String currentPassword, @NotBlank(message = "New password is required") String newPassword);

    UserStatisticsDTO getUserStatistics();

    UserDTO updateUserStatus(UUID id, boolean enabled);

    Page<UserDTO> getFilteredUsers(List<String> roles, String registrationDate, Integer minPoints, Integer maxPoints, Boolean enabled, String activityLevel, Pageable pageable);

    UserDTO updateUserRoles(UUID userId, List<String> roles);
}
