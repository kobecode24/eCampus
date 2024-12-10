package org.doctech.documentation.repository;

import org.doctech.documentation.model.DocumentationComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentationCommentRepository extends JpaRepository<DocumentationComment, UUID> {
    List<DocumentationComment> findByDocumentationId(UUID documentationId);
    Page<DocumentationComment> findByAuthorId(UUID authorId, Pageable pageable);
    void deleteByDocumentationId(UUID documentationId);
}
