package org.doctech.blog.controller;

import lombok.RequiredArgsConstructor;
import org.doctech.blog.dto.BlogCommentDTO;
import org.doctech.blog.service.BlogCommentService;
import org.doctech.common.dto.ApiResponse;
import org.doctech.common.dto.PagedResponse;
import org.doctech.security.model.SecurityUser;
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
@RequestMapping("/blogs/{blogId}/comments")
@RequiredArgsConstructor
public class BlogCommentController {

    private final BlogCommentService blogCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> createComment(
            @PathVariable UUID blogId,
            @Valid @RequestBody BlogCommentDTO commentDTO,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        commentDTO.setAuthorId(securityUser.getId());
        
        BlogCommentDTO createdComment = blogCommentService.createComment(blogId, commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        true,
                        "Comment created successfully",
                        createdComment
                ));
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("@blogCommentService.isCommentAuthor(#commentId, authentication.principal)")
    public ResponseEntity<ApiResponse> updateComment(
            @PathVariable UUID blogId,
            @PathVariable UUID commentId,
            @Valid @RequestBody BlogCommentDTO commentDTO,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        commentDTO.setId(commentId);
        commentDTO.setAuthorId(securityUser.getId());
        commentDTO.setBlogId(blogId);

        BlogCommentDTO updatedComment = blogCommentService.updateComment(commentId, commentDTO);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Comment updated successfully",
                updatedComment
        ));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse> getComment(
            @PathVariable UUID blogId,
            @PathVariable UUID commentId) {
        BlogCommentDTO comment = blogCommentService.getCommentById(commentId);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Comment retrieved successfully",
                comment
        ));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("@blogCommentService.isCommentAuthorOrAdmin(#commentId, authentication.principal)")
    public ResponseEntity<ApiResponse> deleteComment(
            @PathVariable UUID blogId,
            @PathVariable UUID commentId) {
        blogCommentService.deleteComment(commentId);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Comment deleted successfully",
                null
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getBlogComments(
            @PathVariable UUID blogId,
            Pageable pageable) {
        Page<BlogCommentDTO> comments = blogCommentService.getCommentsByBlogId(blogId, pageable);
        PagedResponse<BlogCommentDTO> response = PagedResponse.of(comments.getContent(), comments);
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
        Page<BlogCommentDTO> comments = blogCommentService.getCommentsByAuthor(userId, pageable);
        PagedResponse<BlogCommentDTO> response = PagedResponse.of(comments.getContent(), comments);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "User comments retrieved successfully",
                response
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse> getRecentComments(
            @PathVariable UUID blogId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<BlogCommentDTO> comments = blogCommentService.getCommentsByBlogId(blogId, pageable);
        PagedResponse<BlogCommentDTO> response = PagedResponse.of(comments.getContent(), comments);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Recent comments retrieved successfully",
                response
        ));
    }

}