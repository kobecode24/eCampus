package org.doctech.blog.repository;

import org.doctech.blog.model.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, UUID> {
    List<BlogComment> findByBlogId(UUID blogId);

    Page<BlogComment> findByAuthorId(UUID authorId, Pageable pageable);

    @Query("SELECT c FROM BlogComment c WHERE c.blog.id = :blogId ORDER BY c.createdAt DESC")
    Page<BlogComment> findRecentComments(UUID blogId, Pageable pageable);
}