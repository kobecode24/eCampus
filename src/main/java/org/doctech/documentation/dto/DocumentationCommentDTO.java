package org.doctech.documentation.dto;

import jakarta.validation.constraints.NotBlank;
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
public class DocumentationCommentDTO {
    private UUID id;

    @NotBlank(message = "Content is required")
    private String content;

    private UUID documentationId;
    private UUID authorId;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}
