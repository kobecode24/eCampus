package org.doctech.blog.service;

import org.doctech.blog.dto.BlogCommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BlogCommentService {
    BlogCommentDTO createComment(UUID blogId, BlogCommentDTO commentDTO);

    BlogCommentDTO updateComment(UUID id, BlogCommentDTO commentDTO);

    BlogCommentDTO getCommentById(UUID id);

    void deleteComment(UUID id);

    Page<BlogCommentDTO> getCommentsByBlog(UUID blogId, Pageable pageable);

    Page<BlogCommentDTO> getCommentsByUser(UUID userId, Pageable pageable);

    boolean isCommentAuthor(UUID commentId, Object principal);

    boolean isCommentAuthorOrAdmin(UUID commentId, Object principal);

    Page<BlogCommentDTO> getCommentsByBlogId(UUID blogId, Pageable pageable);

    Page<BlogCommentDTO> getCommentsByAuthor(UUID authorId, Pageable pageable);
}