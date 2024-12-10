package org.doctech.points.mapper;

import org.doctech.points.dto.PointTransactionDTO;
import org.doctech.points.model.PointTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PointTransactionMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    PointTransactionDTO toDTO(PointTransaction transaction);

    @Mapping(target = "user", ignore = true)
    PointTransaction toEntity(PointTransactionDTO dto);
}