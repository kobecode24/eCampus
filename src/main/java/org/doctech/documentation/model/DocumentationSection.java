package org.doctech.documentation.model;


import jakarta.persistence.*;
import lombok.*;
import org.doctech.common.model.Auditable;

import java.util.UUID;

@Entity
@Table(name = "documentation_sections")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationSection extends Auditable {
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "documentation_id", nullable = false)
    private Documentation documentation;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false)
    private Integer orderIndex;
    
    @Column(nullable = false)
    private String sectionId; // For anchor linking (e.g., #installation)
}
