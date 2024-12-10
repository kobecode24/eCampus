package org.doctech.course.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "learning_paths")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class LearningPath {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<LearningPathCourse> learningPathCourses = new ArrayList<>();

    @Column(name = "total_points")
    private Integer totalPoints;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficulty;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        calculateTotalPoints();
        validateFields();
    }

    @PreUpdate
    protected void onPreUpdate() {
        calculateTotalPoints();
        validateFields();
    }

    private void calculateTotalPoints() {
        this.totalPoints = learningPathCourses.stream()
                .map(LearningPathCourse::getCourse)
                .mapToInt(Course::getPointsToEarn)
                .sum();
    }

    private void validateFields() {
        if (totalPoints < 0) {
            throw new IllegalStateException("Total points cannot be negative");
        }
    }

    public void addCourse(Course course, Integer sequenceOrder) {
        LearningPathCourse learningPathCourse = LearningPathCourse.builder()
                .learningPath(this)
                .course(course)
                .sequenceOrder(sequenceOrder)
                .build();
        learningPathCourses.add(learningPathCourse);
        course.addToLearningPath(learningPathCourse);
    }

    public void removeCourse(Course course) {
        learningPathCourses.stream()
                .filter(lpc -> lpc.getCourse().equals(course))
                .findFirst()
                .ifPresent(lpc -> {
                    learningPathCourses.remove(lpc);
                    course.removeFromLearningPath(lpc);
                });
        calculateTotalPoints();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        LearningPath that = (LearningPath) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}