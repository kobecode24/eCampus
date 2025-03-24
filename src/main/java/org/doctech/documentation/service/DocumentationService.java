package org.doctech.documentation.service;

import org.doctech.documentation.dto.DocumentationDTO;
import org.doctech.documentation.dto.DocumentationSectionDTO;
import org.doctech.documentation.model.Documentation;
import org.doctech.documentation.model.DocumentationSection;
import org.doctech.documentation.model.DocumentationStatus;
import org.doctech.documentation.model.TechnologyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;
import java.util.List;

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
    void incrementViews(UUID id);
    void deleteDocumentation(UUID id);
    List<DocumentationSectionDTO> getDocumentationSections(UUID docId);
    DocumentationSectionDTO createSection(UUID docId, DocumentationSectionDTO sectionDTO);
    DocumentationSectionDTO updateSection(UUID sectionId, DocumentationSectionDTO sectionDTO);
    void updateDocumentationStatus(UUID docId, DocumentationStatus status);
    Documentation createDocumentationFromTemplate(String title, String description, TechnologyType tech, UUID id);

    long countByStatus(DocumentationStatus documentationStatus);

    List<DocumentationDTO> getDocumentationByStatus(DocumentationStatus documentationStatus, int i, int i1);

    Map<String, Long> getDocumentStatusDistribution();

    Map<String, Long> getDocumentTechnologyDistribution();
    DocumentationSectionDTO getSectionById(UUID sectionId);
    Page<DocumentationSectionDTO> searchSections(String query, Pageable pageable);
    int getDocumentationReadingTime(UUID docId);
}