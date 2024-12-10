package org.doctech.user.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.*;
import org.doctech.user.dto.UserDTO;
import org.doctech.user.mapper.UserMapper;
import org.doctech.user.model.Badge;
import org.doctech.user.model.Role;
import org.doctech.user.model.User;
import org.doctech.user.model.UserRole;
import org.doctech.user.repository.BadgeRepository;
import org.doctech.user.repository.RoleRepository;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserDTO registerUser(String email, String username, String password, UserRole roleType) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("User with username " + username + " already exists");
        }

        Role role = roleRepository.findByName(roleType.name())
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleType.name()));

        User user = User.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .build();

        user.addRole(role);
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(UserRole roleType) {
        return userRepository.findByRoleName(roleType.name())
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    public UserDTO updateUser(UUID id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equals(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email " + userDTO.getEmail() + " is already in use");
        }

        if (!user.getUsername().equals(userDTO.getUsername()) && userRepository.existsByUsername(userDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username " + userDTO.getUsername() + " is already in use");
        }

        user.setEmail(userDTO.getEmail());
        user.setUsername(userDTO.getUsername());
        user.setEnabled(userDTO.isEnabled());

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO addPoints(UUID userId, int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points to add must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.addPoints(points);
        checkAndUpdateLevel(user);

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public UserDTO spendPoints(UUID userId, int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points to spend must be positive");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!user.spendPoints(points)) {
            throw new InsufficientPointsException("User doesn't have enough points");
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public UserDTO addRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + roleName));

        user.addRole(role);
        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public UserDTO removeRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + roleName));

        if (user.getRoles().size() <= 1) {
            throw new IllegalStateException("Cannot remove the last role from a user");
        }

        user.removeRole(role);
        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    public UserDTO awardBadge(UUID userId, UUID badgeId) {
        User user = userRepository.findByIdWithBadges(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new BadgeNotFoundException("Badge not found with id: " + badgeId));

        user.addBadge(badge);
        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Badge> getUserBadges(UUID userId) {
        User user = userRepository.findByIdWithBadges(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return user.getBadges();
    }

    @Override
    public void updateLastLogin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.updateLastLogin();
        userRepository.save(user);
    }

    @Override
    public UserDTO toggleUserEnabled(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setEnabled(!user.isEnabled());
        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getTopUsersByPoints(int limit) {
        return userRepository.findTopUsersByPoints(limit)
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(UserRole role) {
        return userRepository.countByRoleName(role.name());
    }

    private void checkAndUpdateLevel(User user) {
        int currentLevel = user.getLevel();
        int newLevel = (user.getPoints() / 1000) + 1;

        if (newLevel > currentLevel) {
            user.setLevel(newLevel);
        }
    }
}