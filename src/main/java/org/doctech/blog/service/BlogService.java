package org.doctech.blog.service;

import org.doctech.blog.dto.BlogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface BlogService {
    BlogDTO createBlog(BlogDTO blogDTO);

    BlogDTO updateBlog(UUID id, BlogDTO blogDTO);

    BlogDTO getBlogById(UUID id, UUID currentUserId);

    Page<BlogDTO> getAllBlogs(Pageable pageable, UUID currentUserId);

    Page<BlogDTO> getPublishedBlogs(Pageable pageable, UUID currentUserId);

    Page<BlogDTO> getBlogsByAuthor(UUID authorId, Pageable pageable);

    @Transactional
    BlogDTO toggleLike(UUID blogId, UUID userId);

    Page<BlogDTO> getBlogsByTag(String tag, Pageable pageable, UUID currentUserId);

    BlogDTO publishBlog(UUID id);

    Page<BlogDTO> getMostPopularBlogs(Pageable pageable, UUID currentUserId);

    void deleteBlog(UUID id);

    Page<BlogDTO> searchBlogs(String query, Pageable pageable, UUID currentUserId);

    boolean isAuthorOrAdmin(UUID blogId, Object principal);
}
