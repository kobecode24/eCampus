package org.doctech.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.doctech.user.model.AchievementType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDTO {
    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Points reward is required")
    @PositiveOrZero(message = "Points reward must be zero or positive")
    private Integer pointsReward;

    @NotNull(message = "Achievement type is required")
    private AchievementType type;

    @NotNull(message = "Required progress is required")
    @PositiveOrZero(message = "Required progress must be zero or positive")
    private Integer requiredProgress;

    private String imageUrl;
    private LocalDateTime createdAt;
}