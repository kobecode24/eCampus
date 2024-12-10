package org.doctech.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private Integer totalPoints;
    private Integer currentLevel;
    private Integer completedCoursesCount;
    private Integer completedLearningPathsCount;
    private Integer earnedBadgesCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
