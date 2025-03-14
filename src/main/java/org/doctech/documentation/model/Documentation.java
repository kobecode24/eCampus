package org.doctech.documentation.model;

import jakarta.persistence.*;
import lombok.*;
import org.doctech.common.model.Auditable;
import org.doctech.user.model.User;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "documentation")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Documentation extends Auditable {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ElementCollection
    @CollectionTable(
            name = "documentation_tags",
            joinColumns = @JoinColumn(name = "documentation_id")
    )
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer views = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechnologyType technology;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    @OneToMany(mappedBy = "documentation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<DocumentationComment> comments = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUpdatedAt;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentationStatus status = DocumentationStatus.DRAFT;

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void incrementViews() {
        if (this.views == null) {
            this.views = 0;
        }
        this.views = this.views + 1;
    }

    public void addComment(DocumentationComment comment) {
        comments.add(comment);
        comment.setDocumentation(this);
    }

    public void removeComment(DocumentationComment comment) {
        comments.remove(comment);
        comment.setDocumentation(null);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Documentation that = (Documentation) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
