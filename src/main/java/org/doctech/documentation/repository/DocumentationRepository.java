package org.doctech.documentation.repository;

import org.doctech.documentation.model.Documentation;
import org.doctech.documentation.model.DocumentationStatus;
import org.doctech.documentation.model.TechnologyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentationRepository extends JpaRepository<Documentation, UUID> {
    Page<Documentation> findByTechnology(TechnologyType technology, Pageable pageable);

    @Query("SELECT d FROM Documentation d LEFT JOIN FETCH d.comments WHERE d.id = :id")
    Optional<Documentation> findByIdWithComments(UUID id);

    @Query("SELECT d FROM Documentation d WHERE :tag MEMBER OF d.tags")
    Page<Documentation> findByTag(String tag, Pageable pageable);

    Page<Documentation> findByAuthorId(UUID authorId, Pageable pageable);

    @Query("SELECT d FROM Documentation d ORDER BY d.views DESC")
    Page<Documentation> findMostViewed(Pageable pageable);

    @Query("SELECT d FROM Documentation d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Documentation> search(String query, Pageable pageable);

    long countByStatus(DocumentationStatus status);
    Page<Documentation> findByStatusOrderByLastUpdatedAtDesc(DocumentationStatus status, Pageable pageable);

    long countByTechnology(TechnologyType technology);
}