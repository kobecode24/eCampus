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
public class LearningPathProgressDTO {
    private UUID id;
    private UUID userId;
    private UUID learningPathId;
    private String learningPathTitle;
    private Integer completionPercentage;
    private Integer completedCoursesCount;
    private Integer totalCoursesCount;
    private boolean completed;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
}
