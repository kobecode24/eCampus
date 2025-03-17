package org.doctech.documentation.mapper;

import org.doctech.documentation.dto.DocumentationDTO;
import org.doctech.documentation.model.Documentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {DocumentationCommentMapper.class})
public interface DocumentationMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.username", target = "authorUsername")
    DocumentationDTO toDTO(Documentation documentation);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "status", defaultExpression = "java(org.doctech.documentation.model.DocumentationStatus.DRAFT)")
    Documentation toEntity(DocumentationDTO dto);
}