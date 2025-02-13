package org.doctech.documentation.controller;

import lombok.RequiredArgsConstructor;
import org.doctech.common.dto.ApiResponse;
import org.doctech.common.dto.PagedResponse;
import org.doctech.documentation.dto.DocumentationCommentDTO;
import org.doctech.documentation.service.DocumentationCommentService;
import org.doctech.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/documentation/{documentationId}/comments")
@RequiredArgsConstructor
public class DocumentationCommentController {

    private final DocumentationCommentService commentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> createComment(
            @PathVariable UUID documentationId,
            @Valid @RequestBody DocumentationCommentDTO commentDTO,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        commentDTO.setAuthorId(currentUser.getId());

        DocumentationCommentDTO createdComment = commentService.createComment(documentationId, commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        true,
                        "Comment created successfully",
                        createdComment
                ));
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("@documentationCommentService.isCommentAuthor(#commentId, principal)")
    public ResponseEntity<ApiResponse> updateComment(
            @PathVariable UUID documentationId,
            @PathVariable UUID commentId,
            @Valid @RequestBody DocumentationCommentDTO commentDTO) {
        commentDTO.setId(commentId);
        DocumentationCommentDTO updatedComment = commentService.updateComment(commentId, commentDTO);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Comment updated successfully",
                updatedComment
        ));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse> getComment(
            @PathVariable UUID documentationId,
            @PathVariable UUID commentId) {
        DocumentationCommentDTO comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Comment retrieved successfully",
                comment
        ));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("@documentationCommentService.isCommentAuthorOrAdmin(#commentId, principal)")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable UUID documentationId,
            @PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Comment deleted successfully",
                null
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getDocumentationComments(
            @PathVariable UUID documentationId,
            Pageable pageable) {
        Page<DocumentationCommentDTO> comments =
                commentService.getCommentsByDocumentation(documentationId, pageable);
        PagedResponse<DocumentationCommentDTO> response =
                PagedResponse.of(comments.getContent(), comments);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Comments retrieved successfully",
                response
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserComments(
            @PathVariable UUID userId,
            Pageable pageable) {
        Page<DocumentationCommentDTO> comments =
                commentService.getCommentsByAuthor(userId, pageable);
        PagedResponse<DocumentationCommentDTO> response =
                PagedResponse.of(comments.getContent(), comments);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "User comments retrieved successfully",
                response
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse> getRecentComments(
            @PathVariable UUID documentationId,
            @RequestParam(defaultValue = "10") int limit,
            Pageable pageable) {
        // Create a new Pageable with the specified limit and keep other properties from the original Pageable
        Pageable updatedPageable = PageRequest.of(pageable.getPageNumber(), limit, pageable.getSort());

        Page<DocumentationCommentDTO> comments =
                commentService.getCommentsByDocumentation(
                        documentationId,
                        updatedPageable
                );
        PagedResponse<DocumentationCommentDTO> response =
                PagedResponse.of(comments.getContent(), comments);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Recent comments retrieved successfully",
                response
        ));
    }
}