package org.doctech.documentation.mapper;

import org.doctech.documentation.dto.DocumentationSectionDTO;
import org.doctech.documentation.model.DocumentationSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentationSectionMapper {

    @Mapping(source = "documentation.id", target = "documentationId")
    DocumentationSectionDTO toDTO(DocumentationSection section);

    @Mapping(target = "documentation", ignore = true)
    DocumentationSection toEntity(DocumentationSectionDTO dto);
}