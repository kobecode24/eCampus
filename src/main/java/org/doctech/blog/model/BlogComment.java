package org.doctech.blog.model;

import jakarta.persistence.*;
import lombok.*;
import org.doctech.user.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blog_comments")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class BlogComment {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    @ToString.Exclude
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    private User author;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUpdatedAt;

    @PrePersist
    protected void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void setBlog(Blog blog) {
        Blog oldBlog = this.blog;
        this.blog = blog;

        if (oldBlog != null && oldBlog.getComments() != null) {
            oldBlog.getComments().remove(this);
        }

        if (blog != null && blog.getComments() != null && !blog.getComments().contains(this)) {
            blog.getComments().add(this);
        }
    }
}
