package org.doctech.documentation.controller;

import lombok.RequiredArgsConstructor;
import org.doctech.common.dto.ApiResponse;
import org.doctech.common.dto.PagedResponse;
import org.doctech.common.exception.DocumentationNotFoundException;
import org.doctech.documentation.dto.DocumentationDTO;
import org.doctech.documentation.dto.DocumentationCommentDTO;
import org.doctech.documentation.dto.DocumentationSectionDTO;
import org.doctech.documentation.mapper.DocumentationMapper;
import org.doctech.documentation.model.Documentation;
import org.doctech.documentation.model.DocumentationSection;
import org.doctech.documentation.model.DocumentationStatus;
import org.doctech.documentation.model.TechnologyType;
import org.doctech.documentation.repository.DocumentationRepository;
import org.doctech.documentation.repository.DocumentationSectionRepository;
import org.doctech.documentation.service.DocumentationService;
import org.doctech.documentation.service.DocumentationCommentService;
import org.doctech.security.model.SecurityUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/documentation")
@RequiredArgsConstructor
public class DocumentationController {

    private final DocumentationService documentationService;
    private final DocumentationCommentService commentService;
    public final DocumentationRepository documentationRepository;
    public final DocumentationMapper documentationMapper;
    public final DocumentationSectionRepository sectionRepository;


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse> createDocumentation(
            @Valid @RequestBody DocumentationDTO documentationDTO,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        documentationDTO.setAuthorId(securityUser.getId());

        DocumentationDTO createdDoc = documentationService.createDocumentation(documentationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Documentation created successfully", createdDoc));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse> updateDocumentation(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentationDTO documentationDTO,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        documentationDTO.setId(id);
        documentationDTO.setAuthorId(securityUser.getId());

        DocumentationDTO updatedDoc = documentationService.updateDocumentation(id, documentationDTO);
        return ResponseEntity.ok(new ApiResponse(true, "Documentation updated successfully", updatedDoc));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getDocumentation(@PathVariable UUID id) {
        DocumentationDTO doc = documentationService.getDocumentationById(id);
        documentationService.incrementViews(id);
        return ResponseEntity.ok(new ApiResponse(true, "Documentation retrieved successfully", doc));
    }

    @GetMapping("/sections/{id}")
    public ResponseEntity<ApiResponse> getDocumentationSection(@PathVariable UUID id) {
        DocumentationSectionDTO section = documentationService.getSectionById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Section retrieved successfully", section));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllDocumentation(Pageable pageable) {
        Page<DocumentationDTO> docs = documentationService.getAllDocumentation(pageable);
        PagedResponse<DocumentationDTO> response = PagedResponse.of(docs.getContent(), docs);
        return ResponseEntity.ok(new ApiResponse(true, "Documentation list retrieved successfully", response));
    }

    @GetMapping("/technology/{type}")
    public ResponseEntity<ApiResponse> getDocumentationByTechnology(
            @PathVariable TechnologyType type,
            Pageable pageable) {
        Page<DocumentationDTO> docs = documentationService.getDocumentationByTechnology(type, pageable);
        PagedResponse<DocumentationDTO> response = PagedResponse.of(docs.getContent(), docs);
        return ResponseEntity.ok(new ApiResponse(true, "Documentation by technology retrieved successfully", response));
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<ApiResponse> getDocumentationByTag(
            @PathVariable String tag,
            Pageable pageable) {
        Page<DocumentationDTO> docs = documentationService.getDocumentationByTag(tag, pageable);
        PagedResponse<DocumentationDTO> response = PagedResponse.of(docs.getContent(), docs);
        return ResponseEntity.ok(new ApiResponse(true, "Documentation by tag retrieved successfully", response));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse> getDocumentationByAuthor(
            @PathVariable UUID authorId,
            Pageable pageable) {
        Page<DocumentationDTO> docs = documentationService.getDocumentationByAuthor(authorId, pageable);
        PagedResponse<DocumentationDTO> response = PagedResponse.of(docs.getContent(), docs);
        return ResponseEntity.ok(new ApiResponse(true, "Documentation by author retrieved successfully", response));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse> getMostViewedDocumentation(Pageable pageable) {
        Page<DocumentationDTO> docs = documentationService.getMostViewedDocumentation(pageable);
        PagedResponse<DocumentationDTO> response = PagedResponse.of(docs.getContent(), docs);
        return ResponseEntity.ok(new ApiResponse(true, "Most viewed documentation retrieved successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchDocumentation(
            @RequestParam String query,
            Pageable pageable) {
        Page<DocumentationDTO> docs = documentationService.searchDocumentation(query, pageable);
        PagedResponse<DocumentationDTO> response = PagedResponse.of(docs.getContent(), docs);
        return ResponseEntity.ok(new ApiResponse(true, "Search results retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse> deleteDocumentation(@PathVariable UUID id) {
        documentationService.deleteDocumentation(id);
        return ResponseEntity.ok(new ApiResponse(true, "Documentation deleted successfully", null));
    }

    // Comment Management Endpoints
    @PostMapping("/{docId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> addComment(
            @PathVariable UUID docId,
            @Valid @RequestBody DocumentationCommentDTO commentDTO) {
        DocumentationCommentDTO createdComment = commentService.createComment(docId, commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Comment added successfully", createdComment));
    }

    @GetMapping("/{docId}/comments")
    public ResponseEntity<ApiResponse> getDocumentationComments(
            @PathVariable UUID docId,
            Pageable pageable) {
        Page<DocumentationCommentDTO> comments = commentService.getCommentsByDocumentation(docId, pageable);
        PagedResponse<DocumentationCommentDTO> response = PagedResponse.of(comments.getContent(), comments);
        return ResponseEntity.ok(new ApiResponse(true, "Comments retrieved successfully", response));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getDocumentationStats() {
        Map<String, Object> stats = new HashMap<>();

        // Count docs by status
        stats.put("totalCount", documentationRepository.count());
        stats.put("publishedCount", documentationRepository.countByStatus(DocumentationStatus.PUBLISHED));
        stats.put("draftCount", documentationRepository.countByStatus(DocumentationStatus.DRAFT));
        stats.put("reviewCount", documentationRepository.countByStatus(DocumentationStatus.REVIEW));

        // Top viewed docs
        List<DocumentationDTO> topViewed = documentationRepository.findMostViewed(PageRequest.of(0, 5))
                .getContent()
                .stream()
                .map(doc -> {
                    DocumentationDTO dto = documentationMapper.toDTO(doc);
                    dto.setSections(documentationService.getDocumentationSections(doc.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        stats.put("topViewed", topViewed);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Documentation statistics retrieved successfully",
                stats
        ));
    }

    @GetMapping("/{docId}/sections")
    public ResponseEntity<ApiResponse> getDocumentationSections(@PathVariable UUID docId) {
        List<DocumentationSectionDTO> sections = documentationService.getDocumentationSections(docId);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Documentation sections retrieved successfully",
                sections
        ));
    }

    @PostMapping("/{docId}/sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'MODERATOR')")
    public ResponseEntity<ApiResponse> createSection(
            @PathVariable UUID docId,
            @Valid @RequestBody DocumentationSectionDTO sectionDTO,
            Authentication authentication) {

        DocumentationSectionDTO createdSection = documentationService.createSection(docId, sectionDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        true,
                        "Section created successfully",
                        createdSection
                ));
    }

    @PutMapping("/sections/{sectionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'MODERATOR')")
    public ResponseEntity<ApiResponse> updateSection(
            @PathVariable UUID sectionId,
            @Valid @RequestBody DocumentationSectionDTO sectionDTO) {

        DocumentationSectionDTO updatedSection = documentationService.updateSection(sectionId, sectionDTO);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Section updated successfully",
                updatedSection
        ));
    }

    @PutMapping("/{docId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'MODERATOR')")
    public ResponseEntity<ApiResponse> updateDocumentationStatus(
            @PathVariable UUID docId,
            @RequestBody Map<String, String> statusData) {

        DocumentationStatus status = DocumentationStatus.valueOf(statusData.get("status"));
        documentationService.updateDocumentationStatus(docId, status);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Documentation status updated successfully",
                null
        ));
    }

    @GetMapping("/workflow/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'MODERATOR')")
    public ResponseEntity<ApiResponse> getWorkflowStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count docs by workflow status
        Map<String, Long> statusCounts = new HashMap<>();
        for (DocumentationStatus status : DocumentationStatus.values()) {
            statusCounts.put(status.name(), documentationRepository.countByStatus(status));
        }
        stats.put("statusCounts", statusCounts);
        
        // Recently updated docs in review
        List<DocumentationDTO> inReview = documentationRepository
            .findByStatusOrderByLastUpdatedAtDesc(DocumentationStatus.REVIEW, PageRequest.of(0, 5))
            .stream()
            .map(documentationMapper::toDTO)
            .collect(Collectors.toList());
        stats.put("inReview", inReview);
        
        // Simple activity metrics
        stats.put("activityMetrics", Map.of(
            "lastDay", 12,
            "lastWeek", 47,
            "lastMonth", 183
        ));
        
        // Average review time (simulated)
        stats.put("averageReviewTime", "2.3 days");
        
        return ResponseEntity.ok(new ApiResponse(
            true, 
            "Workflow statistics retrieved successfully",
            stats
        ));
    }

    @GetMapping("/{docId}/structure")
    public ResponseEntity<ApiResponse> getDocumentStructure(@PathVariable UUID docId) {
        // Make sure the document exists
        Documentation doc = documentationRepository.findById(docId)
            .orElseThrow(() -> new DocumentationNotFoundException("Documentation not found with id: " + docId));
        
        List<DocumentationSectionDTO> sections = documentationService.getDocumentationSections(docId);
        
        // Build structure data
        Map<String, Object> structure = new HashMap<>();
        structure.put("id", doc.getId());
        structure.put("title", doc.getTitle());
        structure.put("status", doc.getStatus());
        structure.put("sections", sections);
        
        // Add hierarchy data
        structure.put("hierarchy", buildHierarchy(sections));
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Document structure retrieved successfully",
            structure
        ));
    }

    // Helper method to build section hierarchy
    private List<Map<String, Object>> buildHierarchy(List<DocumentationSectionDTO> sections) {
        List<Map<String, Object>> hierarchy = new ArrayList<>();
        
        // For now, create a flat hierarchy
        for (DocumentationSectionDTO section : sections) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", section.getId());
            node.put("title", section.getTitle());
            node.put("sectionId", section.getSectionId());
            node.put("order", section.getOrderIndex());
            
            // Add some metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("wordCount", wordCount(section.getContent()));
            metadata.put("hasImages", section.getContent().contains("<img"));
            metadata.put("hasCode", section.getContent().contains("<code>"));
            node.put("metadata", metadata);
            
            hierarchy.add(node);
        }
        
        return hierarchy;
    }

    private int wordCount(String content) {
        // Simple word count by splitting on whitespace
        // Remove HTML tags first
        String noHtml = content.replaceAll("<[^>]*>", "");
        return noHtml.split("\\s+").length;
    }

    @PutMapping("/{docId}/structure/reorder")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'MODERATOR')")
    public ResponseEntity<ApiResponse> reorderSections(
            @PathVariable UUID docId,
            @RequestBody List<Map<String, Object>> orderedSections) {
        
        // Validate document exists
        if (!documentationRepository.existsById(docId)) {
            throw new DocumentationNotFoundException("Documentation not found with id: " + docId);
        }
        
        // Update order index for each section
        for (int i = 0; i < orderedSections.size(); i++) {
            Map<String, Object> sectionData = orderedSections.get(i);
            UUID sectionId = UUID.fromString(sectionData.get("id").toString());
            
            DocumentationSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found: " + sectionId));
            
            section.setOrderIndex(i);
            sectionRepository.save(section);
        }
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Documentation structure updated successfully",
            documentationService.getDocumentationSections(docId)
        ));
    }

    @PostMapping("/templates/{templateType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse> createFromTemplate(
            @PathVariable String templateType,
            @RequestBody Map<String, Object> customizations,
            Authentication authentication) {
        
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        
        // Create the appropriate template based on type
        TechnologyType tech;
        String title = customizations.get("title").toString();
        String description = customizations.getOrDefault("description", "").toString();

        tech = switch (templateType.toLowerCase()) {
            case "react" -> TechnologyType.FRONTEND;
            case "spring" -> TechnologyType.BACKEND;
            case "api" -> TechnologyType.API;
            default -> TechnologyType.LIBRARY;
        };
        
        // Create documentation from template
        Documentation doc = documentationService.createDocumentationFromTemplate(
            title, description, tech, securityUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(
                true, 
                "Documentation created from template",
                documentationMapper.toDTO(doc)
            ));
    }

    @GetMapping("/{docId}/reading-time")
    public ResponseEntity<ApiResponse> getDocumentationReadingTime(@PathVariable UUID docId) {
        int readingTimeMinutes = documentationService.getDocumentationReadingTime(docId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("readingTimeMinutes", readingTimeMinutes);
        response.put("readingTimeFormatted", formatReadingTime(readingTimeMinutes));
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Documentation reading time retrieved successfully",
            response
        ));
    }

    /**
     * Format reading time into a user-friendly string
     */
    private String formatReadingTime(int minutes) {
        if (minutes < 1) {
            return "Less than a minute";
        } else if (minutes == 1) {
            return "1 minute read";
        } else {
            return minutes + " minutes read";
        }
    }

    @GetMapping("/sections/search")
    public ResponseEntity<ApiResponse> searchSections(
            @RequestParam String query,
            Pageable pageable) {
        Page<DocumentationSectionDTO> sections = documentationService.searchSections(query, pageable);
        PagedResponse<DocumentationSectionDTO> response = PagedResponse.of(sections.getContent(), sections);
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Section search results retrieved successfully",
            response
        ));
    }

}