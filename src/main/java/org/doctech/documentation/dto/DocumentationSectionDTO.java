package org.doctech.documentation.dto;


import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationSectionDTO {
    private UUID id;
    private UUID documentationId;
    private String title;
    private String content;
    private Integer orderIndex;
    private String sectionId;
}