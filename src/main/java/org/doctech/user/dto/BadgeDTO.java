package org.doctech.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.doctech.user.model.BadgeType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDTO {
    private UUID id;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Badge type is required")
    private BadgeType type;

    @PositiveOrZero(message = "Points required must be zero or positive")
    private Integer pointsRequired;

    @PositiveOrZero(message = "Points cost must be zero or positive")
    private Integer pointsCost;

    private LocalDateTime createdAt;
}