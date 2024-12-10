package org.doctech.blog.service;

import org.doctech.blog.dto.BlogCommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BlogCommentService {
    BlogCommentDTO createComment(UUID blogId, BlogCommentDTO commentDTO);
    BlogCommentDTO updateComment(UUID id, BlogCommentDTO commentDTO);
    BlogCommentDTO getCommentById(UUID id);
    Page<BlogCommentDTO> getBlogComments(UUID blogId, Pageable pageable);
    Page<BlogCommentDTO> getUserComments(UUID userId, Pageable pageable);
    void deleteComment(UUID id);
}
