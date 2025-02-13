package org.doctech.blog.repository;

import org.doctech.blog.model.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID> {
    @Query("SELECT b FROM Blog b LEFT JOIN FETCH b.comments WHERE b.id = :id")
    Optional<Blog> findByIdWithComments(UUID id);

    Page<Blog> findByAuthorId(UUID authorId, Pageable pageable);

    Page<Blog> findByPublishedTrue(Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE :tag MEMBER OF b.tags AND b.published = true")
    Page<Blog> findByTag(String tag, Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.published = true ORDER BY b.likes DESC")
    Page<Blog> findAllByOrderByLikesDesc(Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE b.published = true AND " +
            "(LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Blog> search(String query, Pageable pageable);
}