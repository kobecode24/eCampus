package org.doctech.blog.mapper;

import org.doctech.blog.dto.BlogCommentDTO;
import org.doctech.blog.model.BlogComment;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BlogCommentMapper {

    @Mapping(source = "blog.id", target = "blogId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.username", target = "authorUsername")
    @Mapping(source = "author.avatar", target = "authorAvatarUrl")
    BlogCommentDTO toDTO(BlogComment comment);

    @Mapping(target = "blog", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    BlogComment toEntity(BlogCommentDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "blog", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdatedAt", ignore = true)
    void updateEntityFromDTO(BlogCommentDTO dto, @MappingTarget BlogComment comment);
}