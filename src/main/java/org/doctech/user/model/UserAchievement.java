package org.doctech.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_achievements")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class UserAchievement {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achievement_id", nullable = false)
    @ToString.Exclude
    private Achievement achievement;

    @Column(name = "current_progress", nullable = false)
    @Builder.Default
    private Integer currentProgress = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onPrePersist() {
        this.startedAt = LocalDateTime.now();
        validateFields();
    }

    @PreUpdate
    protected void onPreUpdate() {
        validateFields();
        if (this.currentProgress >= this.achievement.getRequiredProgress() && !this.completed) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    private void validateFields() {
        if (currentProgress < 0) {
            throw new IllegalStateException("Current progress cannot be negative");
        }
        if (achievement != null && currentProgress > achievement.getRequiredProgress()) {
            throw new IllegalStateException("Current progress cannot exceed required progress");
        }
    }

    public void updateProgress(int progress) {
        this.currentProgress = progress;
        if (this.currentProgress >= this.achievement.getRequiredProgress() && !this.completed) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserAchievement that = (UserAchievement) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
