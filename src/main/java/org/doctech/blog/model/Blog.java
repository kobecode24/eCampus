package org.doctech.blog.model;

import jakarta.persistence.*;
import lombok.*;
import org.doctech.user.model.User;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "blogs")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Blog {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    @ElementCollection
    @CollectionTable(
            name = "blog_tags",
            joinColumns = @JoinColumn(name = "blog_id")
    )
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Builder.Default
    private Integer likes = 0;

    @Column(name = "points_cost")
    private Integer pointsCost;

    @Column(nullable = false)
    @Builder.Default
    private boolean published = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUpdatedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<BlogComment> comments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "blog_likes",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    @ToString.Exclude
    private Set<User> likedBy = new HashSet<>();

    public boolean isPublished() {
        return this.published;
    }

    public List<BlogComment> getComments() {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        return comments;
    }

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void publish() {
        this.published = true;
        this.publishedAt = LocalDateTime.now();
    }

    public void addComment(BlogComment comment) {
        comments.add(comment);
        comment.setBlog(this);
    }

    public void removeComment(BlogComment comment) {
        comments.remove(comment);
        comment.setBlog(null);
    }

    public void addLike(User user) {
        if (likedBy.add(user)) {
            this.likes++;
        }
    }

    public void removeLike(User user) {
        if (likedBy.remove(user)) {
            this.likes--;
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Blog blog = (Blog) o;
        return getId() != null && Objects.equals(getId(), blog.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}