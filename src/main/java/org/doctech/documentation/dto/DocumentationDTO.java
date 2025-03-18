package org.doctech.documentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.doctech.documentation.model.DocumentationStatus;
import org.doctech.documentation.model.TechnologyType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationDTO {
    private UUID id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Builder.Default
    private Set<String> tags = new HashSet<>();

    private Integer views;

    @NotNull(message = "Technology type is required")
    private TechnologyType technology;

    private UUID authorId;
    private String authorUsername;
    private List<DocumentationCommentDTO> comments;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private UUID lastModifiedBy;
    private Long version;
    private DocumentationStatus status;
    private List<DocumentationSectionDTO> sections;
}