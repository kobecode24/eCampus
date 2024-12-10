package org.doctech.documentation.mapper;

import org.doctech.documentation.dto.DocumentationCommentDTO;
import org.doctech.documentation.model.DocumentationComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentationCommentMapper {

    @Mapping(source = "documentation.id", target = "documentationId")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.username", target = "authorUsername")
    DocumentationCommentDTO toDTO(DocumentationComment comment);

    @Mapping(target = "documentation", ignore = true)
    @Mapping(target = "author", ignore = true)
    DocumentationComment toEntity(DocumentationCommentDTO dto);
}