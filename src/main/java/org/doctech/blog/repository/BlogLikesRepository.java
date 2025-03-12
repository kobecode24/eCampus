package org.doctech.blog.repository;

import org.doctech.blog.model.BlogLike;
import org.doctech.blog.model.BlogLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BlogLikesRepository extends JpaRepository<BlogLike, BlogLikeId> {
    boolean existsByBlogIdAndUserId(UUID blogId, UUID userId);
    Optional<BlogLike> findByBlogIdAndUserId(UUID blogId, UUID userId);

    @Modifying
    @Query("DELETE FROM BlogLike bl WHERE bl.blog.id = :blogId AND bl.user.id = :userId")
    void deleteByBlogIdAndUserId(@Param("blogId") UUID blogId, @Param("userId") UUID userId);
}