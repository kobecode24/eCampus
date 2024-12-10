package org.doctech.user.mapper;

import org.doctech.user.dto.BadgeDTO;
import org.doctech.user.model.Badge;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BadgeMapper {
    BadgeDTO toDTO(Badge badge);
    Badge toEntity(BadgeDTO dto);
}