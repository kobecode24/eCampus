package org.doctech.course.model;

import jakarta.persistence.*;
import lombok.*;
import org.doctech.user.model.User;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "course_progress")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class CourseProgress {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @ToString.Exclude
    private Course course;

    @Column(name = "completion_percentage", nullable = false)
    @Builder.Default
    private Integer completionPercentage = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastAccessedAt;

    @PrePersist
    protected void onPrePersist() {
        this.startedAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        validateFields();
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.lastAccessedAt = LocalDateTime.now();
        validateFields();

        if (this.completionPercentage == 100 && !this.completed) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }

    private void validateFields() {
        if (completionPercentage < 0 || completionPercentage > 100) {
            throw new IllegalStateException("Completion percentage must be between 0 and 100");
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CourseProgress that = (CourseProgress) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
