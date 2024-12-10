package org.doctech.blog.service;

import org.doctech.blog.dto.BlogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface BlogService {
    BlogDTO createBlog(BlogDTO blogDTO);
    BlogDTO updateBlog(UUID id, BlogDTO blogDTO);
    BlogDTO getBlogById(UUID id);
    BlogDTO publishBlog(UUID id);
    Page<BlogDTO> getPublishedBlogs(Pageable pageable);
    Page<BlogDTO> getBlogsByAuthor(UUID authorId, Pageable pageable);
    Page<BlogDTO> getBlogsByTag(String tag, Pageable pageable);
    Page<BlogDTO> getMostPopularBlogs(Pageable pageable);
    Page<BlogDTO> searchBlogs(String query, Pageable pageable);
    BlogDTO likeBlog(UUID blogId, UUID userId);
    BlogDTO unlikeBlog(UUID blogId, UUID userId);
    void deleteBlog(UUID id);
}
