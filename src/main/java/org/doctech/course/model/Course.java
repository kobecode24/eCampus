package org.doctech.course.model;

import jakarta.persistence.*;
import lombok.*;
import org.doctech.user.model.User;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Column(name = "points_to_earn")
    private Integer pointsToEarn;

    @Column(name = "points_cost")
    private Integer pointsCost;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    @ToString.Exclude
    private User instructor;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<LearningPathCourse> learningPathCourses = new ArrayList<>();

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        validateFields();
    }

    @PreUpdate
    protected void onPreUpdate() {
        validateFields();
    }

    private void validateFields() {
        if (pointsToEarn != null && pointsToEarn < 0) {
            throw new IllegalStateException("Points to earn cannot be negative");
        }
        if (pointsCost != null && pointsCost < 0) {
            throw new IllegalStateException("Points cost cannot be negative");
        }
    }

    public void addToLearningPath(LearningPathCourse learningPathCourse) {
        learningPathCourses.add(learningPathCourse);
        learningPathCourse.setCourse(this);
    }

    public void removeFromLearningPath(LearningPathCourse learningPathCourse) {
        learningPathCourses.remove(learningPathCourse);
        learningPathCourse.setCourse(null);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Course course = (Course) o;
        return getId() != null && Objects.equals(getId(), course.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
