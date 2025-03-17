package org.doctech.blog.model;

import jakarta.persistence.*;
import lombok.*;
import org.doctech.common.model.Auditable;
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
public class Blog extends Auditable {

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "blog_likes",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_blog_likes",
                    columnNames = {"blog_id", "user_id"}
            )
    )
    @ToString.Exclude
    private Set<User> likedBy = new HashSet<>();

    @Column(name = "likes_count", nullable = false)
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

    @OneToMany(
            mappedBy = "blog",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    @ToString.Exclude
    private List<BlogComment> comments = new ArrayList<>();

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        if (this.likes == null) {
            this.likes = 0;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void publish() {
        if (!this.published) {
            this.published = true;
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void unpublish() {
        this.published = false;
        this.publishedAt = null;
    }

    public List<BlogComment> getComments() {
        return comments != null ? comments : new ArrayList<>();
    }

    public void addComment(BlogComment comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
        comment.setBlog(this);
    }

    public void removeComment(BlogComment comment) {
        if (comments != null) {
            comments.remove(comment);
            comment.setBlog(null);
        }
    }

    public boolean toggleLike(User user) {
        if (likedBy.contains(user)) {
            likedBy.remove(user);
            likes = Math.max(0, likes - 1);
            return false;
        } else {
            likedBy.add(user);
            likes++;
            return true;
        }
    }

    public boolean hasUserLiked(User user) {
        return likedBy.contains(user);
    }

    public int getLikesCount() {
        return likes != null ? likes : 0;
    }

    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }
        tags.add(tag);
    }

    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }
}