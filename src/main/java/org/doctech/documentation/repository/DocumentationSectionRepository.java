package org.doctech.documentation.repository;

import org.doctech.documentation.model.DocumentationSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DocumentationSectionRepository extends JpaRepository<DocumentationSection, UUID> {
    List<DocumentationSection> findByDocumentationIdOrderByOrderIndex(UUID documentationId);
    
    @Query("SELECT s FROM DocumentationSection s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<DocumentationSection> searchSections(@Param("query") String query, Pageable pageable);
}
