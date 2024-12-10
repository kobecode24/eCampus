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
public class LearningPathCourseDTO {
    private UUID id;
    private UUID learningPathId;
    private UUID courseId;
    private String courseTitle;
    private Integer sequenceOrder;
    private LocalDateTime createdAt;
}
