package org.doctech.blog.mapper;

import org.doctech.blog.dto.BlogDTO;
import org.doctech.blog.model.Blog;
import org.doctech.user.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {BlogCommentMapper.class})
public interface BlogMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.username", target = "authorUsername")
    @Mapping(target = "likes", expression = "java(mapLikedByToLikes(blog.getLikedBy()))")
    @Mapping(source = "comments", target = "comments")
    BlogDTO toDTO(Blog blog);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "likedBy", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    Blog toEntity(BlogDTO blogDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "likedBy", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    void updateEntityFromDTO(BlogDTO blogDTO, @MappingTarget Blog blog);

    default Integer mapLikedByToLikes(java.util.Set<User> likedBy) {
        return likedBy != null ? likedBy.size() : 0;
    }
}