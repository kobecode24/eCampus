package org.doctech.documentation.repository;

import org.doctech.documentation.model.DocumentationSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentationSectionRepository extends JpaRepository<DocumentationSection, UUID> {
    List<DocumentationSection> findByDocumentationIdOrderByOrderIndex(UUID documentationId);
}
