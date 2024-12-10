package org.doctech.documentation.service;

import org.doctech.documentation.dto.DocumentationCommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DocumentationCommentService {
    DocumentationCommentDTO createComment(UUID documentationId, DocumentationCommentDTO commentDTO);
    DocumentationCommentDTO updateComment(UUID id, DocumentationCommentDTO commentDTO);
    DocumentationCommentDTO getCommentById(UUID id);
    List<DocumentationCommentDTO> getCommentsByDocumentationId(UUID documentationId);
    Page<DocumentationCommentDTO> getCommentsByAuthor(UUID authorId, Pageable pageable);
    void deleteComment(UUID id);
}
