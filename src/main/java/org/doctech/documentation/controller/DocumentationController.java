package org.doctech.documentation.controller;

import lombok.RequiredArgsConstructor;
import org.doctech.common.dto.ApiResponse;
import org.doctech.common.dto.PagedResponse;
import org.doctech.documentation.dto.DocumentationDTO;
import org.doctech.documentation.dto.DocumentationCommentDTO;
import org.doctech.documentation.model.TechnologyType;
import org.doctech.documentation.service.DocumentationService;
import org.doctech.documentation.service.DocumentationCommentService;
import org.doctech.security.model.SecurityUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/documentation")
@RequiredArgsConstructor
public class DocumentationController {

    private final DocumentationService documentationService;
    private final DocumentationCommentService commentService;

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
}