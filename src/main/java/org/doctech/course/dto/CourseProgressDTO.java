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
public class CourseProgressDTO {
    private UUID id;
    private UUID userId;
    private UUID courseId;
    private String courseTitle;
    private Integer completionPercentage;
    private boolean completed;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
}
