package org.doctech.blog.controller;

import lombok.RequiredArgsConstructor;
import org.doctech.blog.dto.BlogDTO;
import org.doctech.blog.service.BlogService;
import org.doctech.common.dto.ApiResponse;
import org.doctech.points.model.TransactionType;
import org.doctech.points.service.PointTransactionService;
import org.doctech.security.model.SecurityUser;
import org.doctech.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final PointTransactionService pointTransactionService;

    // Blog Creation and Management
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> createBlog(
            @Valid @RequestBody BlogDTO blogDTO,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        blogDTO.setAuthorId(securityUser.getId());

        BlogDTO createdBlog = blogService.createBlog(blogDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Blog created successfully", createdBlog));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@blogService.isAuthorOrAdmin(#id, authentication.principal)")
    public ResponseEntity<ApiResponse> updateBlog(
            @PathVariable UUID id,
            @Valid @RequestBody BlogDTO blogDTO,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        blogDTO.setAuthorId(securityUser.getId());

        BlogDTO updatedBlog = blogService.updateBlog(id, blogDTO);
        return ResponseEntity.ok(new ApiResponse(
                true,
                "Blog updated successfully",
                updatedBlog));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@blogService.isAuthorOrAdmin(#id, principal)")
    public ResponseEntity<ApiResponse> deleteBlog(@PathVariable UUID id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok(new ApiResponse(true, "Blog deleted successfully", null));
    }

    // Blog Publishing
    @PostMapping("/{id}/publish")
    @PreAuthorize("@blogService.isAuthorOrAdmin(#id, principal)")
    public ResponseEntity<ApiResponse> publishBlog(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse(true, "Blog published successfully",
                blogService.publishBlog(id)));
    }

    // Blog Retrieval
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getBlog(@PathVariable UUID id, Authentication authentication) {
        UUID currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            currentUserId = ((SecurityUser) authentication.getPrincipal()).getId();
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Blog retrieved successfully",
                blogService.getBlogById(id, currentUserId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllBlogs(Pageable pageable, Authentication authentication) {
        UUID currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            currentUserId = ((SecurityUser) authentication.getPrincipal()).getId();
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Blogs retrieved successfully",
                blogService.getAllBlogs(pageable, currentUserId)));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse> getPublishedBlogs(Pageable pageable, Authentication authentication) {
        UUID currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            currentUserId = ((SecurityUser) authentication.getPrincipal()).getId();
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Published blogs retrieved successfully",
                blogService.getPublishedBlogs(pageable, currentUserId)));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse> getBlogsByAuthor(
            @PathVariable UUID authorId,
            Pageable pageable) {
        return ResponseEntity.ok(new ApiResponse(true, "Author's blogs retrieved successfully",
                blogService.getBlogsByAuthor(authorId, pageable)));
    }

    // Blog Engagement
    @PostMapping("/{blogId}/toggle-like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> toggleLike(
            @PathVariable UUID blogId,
            Authentication authentication) {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        BlogDTO updatedBlog = blogService.toggleLike(blogId, securityUser.getId());
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Like status updated successfully",
            updatedBlog
        ));
    }

    // Blog Search and Filtering
    @GetMapping("/tag/{tag}")
    public ResponseEntity<ApiResponse> getBlogsByTag(
            @PathVariable String tag,
            Pageable pageable,
            Authentication authentication) {
        UUID currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            currentUserId = ((SecurityUser) authentication.getPrincipal()).getId();
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Tagged blogs retrieved successfully",
                blogService.getBlogsByTag(tag, pageable, currentUserId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchBlogs(
            @RequestParam String query,
            Pageable pageable,
            Authentication authentication) {
        UUID currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            currentUserId = ((SecurityUser) authentication.getPrincipal()).getId();
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Search results retrieved successfully",
                blogService.searchBlogs(query, pageable, currentUserId)));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse> getPopularBlogs(Pageable pageable, Authentication authentication) {
        UUID currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            currentUserId = ((SecurityUser) authentication.getPrincipal()).getId();
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "Popular blogs retrieved successfully",
                blogService.getMostPopularBlogs(pageable, currentUserId)));
    }

    // Helper Methods
    private void validateAndProcessPointsForBlogCreation(UUID userId, Integer pointsCost) {
        if (pointsCost > 0) {
            pointTransactionService.validatePoints(userId, pointsCost);
            pointTransactionService.spendPoints(
                    userId,
                    pointsCost,
                    TransactionType.BLOG_CREATION,
                    "Blog creation cost"
            );
        }
    }
}