package org.doctech.user.service;

import org.doctech.common.exception.*;
import org.doctech.user.dto.UserDTO;
import org.doctech.user.dto.UserStatisticsDTO;
import org.doctech.user.mapper.UserMapper;
import org.doctech.user.model.Badge;
import org.doctech.user.model.Role;
import org.doctech.user.model.User;
import org.doctech.user.model.UserRole;
import org.doctech.user.repository.BadgeRepository;
import org.doctech.user.repository.RoleRepository;
import org.doctech.user.repository.UserRepository;
import org.doctech.blog.repository.BlogRepository;
import org.doctech.blog.repository.BlogCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private BlogCommentRepository commentRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private Role testRole;
    private Badge testBadge;
    private UserDTO testUserDTO;
    private final UUID userId = UUID.randomUUID();
    private final UUID badgeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedpassword")
                .points(100)
                .level(1)
                .enabled(true)
                .roles(new HashSet<>())
                .badges(new ArrayList<>())
                .createdAt(LocalDateTime.now().minusDays(30))
                .build();

        // Setup test role
        testRole = new Role();
        testRole.setId(UUID.randomUUID());
        testRole.setName("STUDENT");

        // Add role to test user
        testUser.addRole(testRole);

        // Setup test badge
        testBadge = new Badge();
        testBadge.setId(badgeId);
        testBadge.setName("Test Badge");
        testBadge.setDescription("Test badge description");

        // Setup test UserDTO
        testUserDTO = new UserDTO();
        testUserDTO.setId(userId);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setPoints(100);
        testUserDTO.setLevel(1);
        testUserDTO.setEnabled(true);
        testUserDTO.setRoles(Set.of("STUDENT"));
    }

    // ----------------- Registration Tests -----------------

    @Test
    void registerUser_Success() {
        // Arrange
        String email = "new@example.com";
        String username = "newuser";
        String password = "password123";
        UserRole roleType = UserRole.STUDENT;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(roleRepository.findByName(roleType.name())).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.registerUser(email, username, password, roleType);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO, result);

        // Verify interactions
        verify(userRepository).existsByEmail(email);
        verify(userRepository).existsByUsername(username);
        verify(roleRepository).findByName(roleType.name());
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDTO(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        String email = "existing@example.com";
        String username = "newuser";
        String password = "password123";
        UserRole roleType = UserRole.STUDENT;

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
                userService.registerUser(email, username, password, roleType));

        // Verify
        verify(userRepository).existsByEmail(email);
        verifyNoMoreInteractions(userRepository, roleRepository, passwordEncoder, userMapper);
    }

    @Test
    void registerUser_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        String email = "new@example.com";
        String username = "existinguser";
        String password = "password123";
        UserRole roleType = UserRole.STUDENT;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
                userService.registerUser(email, username, password, roleType));

        // Verify
        verify(userRepository).existsByEmail(email);
        verify(userRepository).existsByUsername(username);
        verifyNoMoreInteractions(userRepository, roleRepository, passwordEncoder, userMapper);
    }

    @Test
    void registerUser_RoleNotFound_ThrowsException() {
        // Arrange
        String email = "new@example.com";
        String username = "newuser";
        String password = "password123";
        UserRole roleType = UserRole.STUDENT;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(roleRepository.findByName(roleType.name())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoleNotFoundException.class, () ->
                userService.registerUser(email, username, password, roleType));

        // Verify
        verify(userRepository).existsByEmail(email);
        verify(userRepository).existsByUsername(username);
        verify(roleRepository).findByName(roleType.name());
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }

    // ----------------- Get User Tests -----------------

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO, result);

        // Verify
        verify(userRepository).findById(userId);
        verify(userMapper).toDTO(testUser);
    }

    @Test
    void getUserById_NonExistingUser_ThrowsException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
                userService.getUserById(nonExistingId));

        // Verify
        verify(userRepository).findById(nonExistingId);
        verifyNoInteractions(userMapper);
    }

    // ----------------- Update User Tests -----------------

    @Test
    void updateUser_ValidUpdate_ReturnsUpdatedUser() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(updateDTO.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.updateUser(userId, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO, result);

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(updateDTO.getEmail());
        verify(userRepository).existsByUsername(updateDTO.getUsername());
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(updateDTO.getUsername(), capturedUser.getUsername());
        assertEquals(updateDTO.getEmail(), capturedUser.getEmail());
        assertEquals(updateDTO.isEnabled(), capturedUser.isEnabled());
    }

    @Test
    void updateUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("testuser");
        updateDTO.setEmail("existing@example.com");
        updateDTO.setEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateDTO.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () ->
                userService.updateUser(userId, updateDTO));

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail(updateDTO.getEmail());
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    // ----------------- Points Management Tests -----------------

    @Test
    void addPoints_ValidAmount_AddsPointsAndUpdatesLevel() {
        // Arrange
        int pointsToAdd = 1000;
        User updatedUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .points(1100)  // 100 + 1000
                .level(2)      // Level updated
                .build();

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(userId);
        updatedDTO.setPoints(1100);
        updatedDTO.setLevel(2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedDTO);

        // Act
        UserDTO result = userService.addPoints(userId, pointsToAdd);

        // Assert
        assertNotNull(result);
        assertEquals(1100, result.getPoints());
        assertEquals(2, result.getLevel());

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(1100, capturedUser.getPoints());
        assertEquals(2, capturedUser.getLevel());
    }

    @Test
    void addPoints_NegativeAmount_ThrowsException() {
        // Arrange
        int negativePoints = -50;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                userService.addPoints(userId, negativePoints));

        // Verify
        verifyNoInteractions(userRepository, userMapper);
    }

    @Test
    void spendPoints_SufficientPoints_SpendPointsSuccessfully() {
        // Arrange
        int pointsToSpend = 50;
        User updatedUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .points(50)  // 100 - 50
                .level(1)
                .build();

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(userId);
        updatedDTO.setPoints(50);
        updatedDTO.setLevel(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedDTO);

        // Act
        UserDTO result = userService.spendPoints(userId, pointsToSpend);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getPoints());

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(50, capturedUser.getPoints());
    }

    @Test
    void spendPoints_InsufficientPoints_ThrowsException() {
        // Arrange
        int pointsToSpend = 200;  // More than user's 100 points

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(InsufficientPointsException.class, () ->
                userService.spendPoints(userId, pointsToSpend));

        // Verify
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    // ----------------- Role Management Tests -----------------

    @Test
    void addRole_ValidRole_AddsRoleSuccessfully() {
        // Arrange
        String roleName = "INSTRUCTOR";
        Role instructorRole = new Role();
        instructorRole.setId(UUID.randomUUID());
        instructorRole.setName(roleName);

        User updatedUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .roles(new HashSet<>(Set.of(testRole, instructorRole)))
                .build();

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(userId);
        updatedDTO.setRoles(Set.of("STUDENT", "INSTRUCTOR"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(instructorRole));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedDTO);

        // Act
        UserDTO result = userService.addRole(userId, roleName);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().contains("INSTRUCTOR"));

        // Verify
        verify(userRepository).findById(userId);
        verify(roleRepository).findByName(roleName);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertTrue(capturedUser.getRoles().stream().anyMatch(r -> r.getName().equals(roleName)));
    }

    @Test
    void addRole_RoleNotFound_ThrowsException() {
        // Arrange
        String nonExistingRoleName = "NON_EXISTING_ROLE";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(nonExistingRoleName)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RoleNotFoundException.class, () ->
                userService.addRole(userId, nonExistingRoleName));

        // Verify
        verify(userRepository).findById(userId);
        verify(roleRepository).findByName(nonExistingRoleName);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    // ----------------- Badge Management Tests -----------------

    @Test
    void awardBadge_ValidBadge_AddsBadgeSuccessfully() {
        // Arrange
        User userWithBadges = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .badges(new ArrayList<>())
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .badges(List.of(testBadge))
                .build();

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(userId);
        updatedDTO.setBadges(List.of("Test Badge"));
        updatedDTO.setBadgeIds(List.of(badgeId));

        when(userRepository.findByIdWithBadges(userId)).thenReturn(Optional.of(userWithBadges));
        when(badgeRepository.findById(badgeId)).thenReturn(Optional.of(testBadge));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedDTO);

        // Act
        UserDTO result = userService.awardBadge(userId, badgeId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getBadges().contains("Test Badge"));
        assertTrue(result.getBadgeIds().contains(badgeId));

        // Verify
        verify(userRepository).findByIdWithBadges(userId);
        verify(badgeRepository).findById(badgeId);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertTrue(capturedUser.getBadges().contains(testBadge));
    }

    @Test
    void awardBadge_BadgeNotFound_ThrowsException() {
        // Arrange
        UUID nonExistingBadgeId = UUID.randomUUID();

        when(userRepository.findByIdWithBadges(userId)).thenReturn(Optional.of(testUser));
        when(badgeRepository.findById(nonExistingBadgeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadgeNotFoundException.class, () ->
                userService.awardBadge(userId, nonExistingBadgeId));

        // Verify
        verify(userRepository).findByIdWithBadges(userId);
        verify(badgeRepository).findById(nonExistingBadgeId);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    // ----------------- Password Management Tests -----------------

    @Test
    void updatePassword_CorrectCurrentPassword_UpdatesPasswordSuccessfully() {
        // Arrange
        String currentPassword = "currentpassword";
        String newPassword = "newpassword";
        String encodedNewPassword = "encodednewpassword";
        String originalPasswordHash = testUser.getPasswordHash(); // Assume this is "hashedpassword"

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, originalPasswordHash)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // Act
        userService.updatePassword(userId, currentPassword, newPassword);

        // Assert & Verify
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(currentPassword, originalPasswordHash); // Verify against original hash
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(encodedNewPassword, capturedUser.getPasswordHash());
    }

    @Test
    void updatePassword_IncorrectCurrentPassword_ThrowsException() {
        // Arrange
        String wrongCurrentPassword = "wrongpassword";
        String newPassword = "newpassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongCurrentPassword, testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () ->
                userService.updatePassword(userId, wrongCurrentPassword, newPassword));

        // Verify
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(wrongCurrentPassword, testUser.getPasswordHash());
        verifyNoMoreInteractions(passwordEncoder, userRepository);
    }

    // ----------------- User Status Management Tests -----------------

    @Test
    void updateUserStatus_NonAdminUser_UpdatesStatusSuccessfully() {
        // Arrange
        boolean newStatus = false;
        User userToUpdate = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .enabled(true)
                .roles(new HashSet<>(Set.of(testRole)))  // Only STUDENT role
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .enabled(false)
                .roles(new HashSet<>(Set.of(testRole)))
                .build();

        UserDTO updatedDTO = new UserDTO();
        updatedDTO.setId(userId);
        updatedDTO.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedDTO);

        // Act
        UserDTO result = userService.updateUserStatus(userId, newStatus);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEnabled());

        // Verify
        verify(userRepository).findById(userId);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertFalse(capturedUser.isEnabled());
    }

    @Test
    void updateUserStatus_AdminUser_DisableAttempt_ThrowsException() {
        // Arrange
        boolean newStatus = false;
        Role adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("ADMIN");

        User adminUser = User.builder()
                .id(userId)
                .username("adminuser")
                .email("admin@example.com")
                .enabled(true)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUser));

        // Act & Assert
        assertThrows(IllegalOperationException.class, () ->
                userService.updateUserStatus(userId, newStatus));

        // Verify
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    // ----------------- User Statistics Tests -----------------

    @Test
    void getUserStatistics_ReturnsCorrectStatistics() {
        // Arrange
        long activeUsers = 100;
        long newUsers = 10;
        long students = 80;
        long instructors = 20;

        List<User> allUsers = new ArrayList<>();
        allUsers.add(testUser);

        when(userRepository.countByCredentialsNonExpired(true)).thenReturn(activeUsers);
        when(userRepository.countByCreatedAtAfter(any())).thenReturn(newUsers);
        when(userRepository.countByRoleName("STUDENT")).thenReturn(students);
        when(userRepository.countByRoleName("INSTRUCTOR")).thenReturn(instructors);
        when(userRepository.findAll()).thenReturn(allUsers);
        when(blogRepository.countByAuthorId(any())).thenReturn(5L);
        when(commentRepository.countByAuthorId(any())).thenReturn(10L);

        // Act
        UserStatisticsDTO result = userService.getUserStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(activeUsers, result.getTotalActiveUsers());
        assertEquals(newUsers, result.getNewUsersToday());
        assertEquals(students, result.getTotalStudents());
        assertEquals(instructors, result.getTotalInstructors());
        assertTrue(result.getAvgEngagement() > 0);

        // Verify
        verify(userRepository).countByCredentialsNonExpired(true);
        verify(userRepository).countByCreatedAtAfter(any());
        verify(userRepository).countByRoleName("STUDENT");
        verify(userRepository).countByRoleName("INSTRUCTOR");
        verify(userRepository).findAll();
        verify(blogRepository, times(allUsers.size())).countByAuthorId(any());
        verify(commentRepository, times(allUsers.size())).countByAuthorId(any());
    }

}