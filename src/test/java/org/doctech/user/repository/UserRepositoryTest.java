package org.doctech.user.repository;

import org.doctech.user.model.Badge;
import org.doctech.user.model.BadgeType;
import org.doctech.user.model.Role;
import org.doctech.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private Role studentRole;
    private Role adminRole;
    private Badge testBadge;

    @BeforeEach
    void setUp() {
        // Create roles
        studentRole = new Role();
        studentRole.setName("STUDENT");
        entityManager.persist(studentRole);

        adminRole = new Role();
        adminRole.setName("ADMIN");
        entityManager.persist(adminRole);

        // Create badge with required type field set
        testBadge = new Badge();
        testBadge.setName("Test Badge");
        testBadge.setDescription("Test badge description");
        testBadge.setType(BadgeType.ACHIEVEMENT); // Set the required type field
        entityManager.persist(testBadge);

        // Create test users
        testUser1 = User.builder()
                .username("testuser1")
                .email("user1@example.com")
                .passwordHash("password1hash")
                .points(100)
                .level(1)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now().minusDays(30))
                .build();
        testUser1.addRole(studentRole);
        testUser1.addBadge(testBadge);
        entityManager.persist(testUser1);

        testUser2 = User.builder()
                .username("testuser2")
                .email("user2@example.com")
                .passwordHash("password2hash")
                .points(200)
                .level(2)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(false)
                .createdAt(LocalDateTime.now().minusDays(15))
                .build();
        testUser2.addRole(adminRole);
        entityManager.persist(testUser2);

        testUser3 = User.builder()
                .username("testuser3")
                .email("user3@example.com")
                .passwordHash("password3hash")
                .points(50)
                .level(1)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now().minusHours(5))
                .build();
        testUser3.addRole(studentRole);
        entityManager.persist(testUser3);

        entityManager.flush();
    }

    @Test
    void findByEmail_ExistingEmail_ShouldReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("user1@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser1");
    }

    @Test
    void findByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findByUsername_ExistingUsername_ShouldReturnUser() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser2");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("user2@example.com");
    }

    @Test
    void findByUsername_NonExistingUsername_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistentuser");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByEmail_ExistingEmail_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsByEmail("user3@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_NonExistingEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByUsername_ExistingUsername_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsByUsername("testuser1");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_NonExistingUsername_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByRoleName_ExistingRole_ShouldReturnUserList() {
        // When
        List<User> users = userRepository.findByRoleName("STUDENT");

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername).containsExactlyInAnyOrder("testuser1", "testuser3");
    }

    @Test
    void findByRoleName_NonExistingRole_ShouldReturnEmptyList() {
        // When
        List<User> users = userRepository.findByRoleName("NONEXISTENT_ROLE");

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    void countByRoleName_ExistingRole_ShouldReturnCount() {
        // When
        long count = userRepository.countByRoleName("ADMIN");

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByRoleName_NonExistingRole_ShouldReturnZero() {
        // When
        long count = userRepository.countByRoleName("NONEXISTENT_ROLE");

        // Then
        assertThat(count).isZero();
    }

    @Test
    void findByIdWithBadges_ExistingUserWithBadges_ShouldReturnUserWithBadges() {
        // When
        Optional<User> foundUser = userRepository.findByIdWithBadges(testUser1.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getBadges()).hasSize(1);
        assertThat(foundUser.get().getBadges().get(0).getName()).isEqualTo("Test Badge");
    }

    @Test
    void findByIdWithBadges_ExistingUserWithoutBadges_ShouldReturnUserWithEmptyBadges() {
        // When
        Optional<User> foundUser = userRepository.findByIdWithBadges(testUser2.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getBadges()).isEmpty();
    }

    @Test
    void findByIdWithBadges_NonExistingUser_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByIdWithBadges(UUID.randomUUID());

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findTopUsersByPoints_ShouldReturnOrderedUsersList() {
        // When
        List<User> topUsers = userRepository.findTopUsersByPoints(2);

        // Then
        assertThat(topUsers).hasSize(2);
        assertThat(topUsers.get(0).getPoints()).isGreaterThanOrEqualTo(topUsers.get(1).getPoints());
        assertThat(topUsers.get(0).getUsername()).isEqualTo("testuser2");  // User with 200 points
        assertThat(topUsers.get(1).getUsername()).isEqualTo("testuser1");  // User with 100 points
    }

    @Test
    void countByCredentialsNonExpired_True_ShouldReturnCount() {
        // When
        long count = userRepository.countByCredentialsNonExpired(true);

        // Then
        assertThat(count).isEqualTo(2);  // testUser1 and testUser3
    }

    @Test
    void countByCredentialsNonExpired_False_ShouldReturnCount() {
        // When
        long count = userRepository.countByCredentialsNonExpired(false);

        // Then
        assertThat(count).isEqualTo(1);  // testUser2
    }

    @Test
    void countByCreatedAtAfter_ShouldFilterCorrectly() {
        // Given - Use a date far in the past and one far in the future
        LocalDateTime farPastDate = LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime farFutureDate = LocalDateTime.of(2099, 12, 31, 23, 59);

        // When
        long countAfterPast = userRepository.countByCreatedAtAfter(farPastDate);
        long countAfterFuture = userRepository.countByCreatedAtAfter(farFutureDate);

        // Then
        assertThat(countAfterPast).isPositive(); // Should find at least one user
        assertThat(countAfterFuture).isZero(); // Should find no users
    }
}