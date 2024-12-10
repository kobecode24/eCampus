package org.doctech.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_progress")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class UserProgress {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentLevel = 1;

    @Column(name = "completed_courses_count", nullable = false)
    @Builder.Default
    private Integer completedCoursesCount = 0;

    @Column(name = "completed_learning_paths_count", nullable = false)
    @Builder.Default
    private Integer completedLearningPathsCount = 0;

    @Column(name = "earned_badges_count", nullable = false)
    @Builder.Default
    private Integer earnedBadgesCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUpdatedAt;

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        validateFields();
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
        validateFields();
    }

    private void validateFields() {
        if (totalPoints < 0) {
            throw new IllegalStateException("Total points cannot be negative");
        }
        if (currentLevel < 1) {
            throw new IllegalStateException("Current level cannot be less than 1");
        }
        if (completedCoursesCount < 0) {
            throw new IllegalStateException("Completed courses count cannot be negative");
        }
        if (completedLearningPathsCount < 0) {
            throw new IllegalStateException("Completed learning paths count cannot be negative");
        }
        if (earnedBadgesCount < 0) {
            throw new IllegalStateException("Earned badges count cannot be negative");
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserProgress that = (UserProgress) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
