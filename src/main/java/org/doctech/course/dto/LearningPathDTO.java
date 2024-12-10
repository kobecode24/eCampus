package org.doctech.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.doctech.course.model.DifficultyLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathDTO {
    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Builder.Default
    private List<LearningPathCourseDTO> courses = new ArrayList<>();

    private Integer totalPoints;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficulty;

    private LocalDateTime createdAt;
}