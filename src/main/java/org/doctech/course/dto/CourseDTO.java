package org.doctech.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.doctech.course.model.CourseStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Status is required")
    private CourseStatus status;

    @PositiveOrZero(message = "Points to earn must be zero or positive")
    private Integer pointsToEarn;

    @PositiveOrZero(message = "Points cost must be zero or positive")
    private Integer pointsCost;

    private LocalDateTime createdAt;
    private UUID instructorId;
    private String instructorUsername;
}
