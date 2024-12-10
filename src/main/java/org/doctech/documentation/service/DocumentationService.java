package org.doctech.documentation.service;

import org.doctech.documentation.dto.DocumentationDTO;
import org.doctech.documentation.model.TechnologyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DocumentationService {
    DocumentationDTO createDocumentation(DocumentationDTO documentationDTO);
    DocumentationDTO updateDocumentation(UUID id, DocumentationDTO documentationDTO);
    DocumentationDTO getDocumentationById(UUID id);
    Page<DocumentationDTO> getAllDocumentation(Pageable pageable);
    Page<DocumentationDTO> getDocumentationByTechnology(TechnologyType technology, Pageable pageable);
    Page<DocumentationDTO> getDocumentationByTag(String tag, Pageable pageable);
    Page<DocumentationDTO> getDocumentationByAuthor(UUID authorId, Pageable pageable);
    Page<DocumentationDTO> getMostViewedDocumentation(Pageable pageable);
    Page<DocumentationDTO> searchDocumentation(String query, Pageable pageable);
    DocumentationDTO incrementViews(UUID id);
    void deleteDocumentation(UUID id);
}