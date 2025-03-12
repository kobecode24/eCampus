package org.doctech.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.doctech.user.model.User;

@Entity
@Table(name = "blog_likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogLike {
    @EmbeddedId
    private BlogLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blogId")
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    private User user;

    public BlogLike(Blog blog, User user) {
        this.id = new BlogLikeId(blog.getId(), user.getId());
        this.blog = blog;
        this.user = user;
    }
} 