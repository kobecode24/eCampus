package org.doctech.user.mapper;

import org.doctech.user.dto.UserDTO;
import org.doctech.user.model.Badge;
import org.doctech.user.model.Role;
import org.doctech.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(source = "badges", target = "badgeIds", qualifiedByName = "badgesToIds")
    @Mapping(source = "badges", target = "badges", qualifiedByName = "badgesToNames")
    UserDTO toDTO(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "badges", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserDTO userDTO);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Named("badgesToIds")
    default List<UUID> badgesToIds(List<Badge> badges) {
        if (badges == null) {
            return null;
        }
        return badges.stream()
                .map(Badge::getId)
                .collect(Collectors.toList());
    }

    @Named("badgesToNames")
    default List<String> badgesToNames(List<Badge> badges) {
        if (badges == null) {
            return null;
        }
        return badges.stream()
                .map(Badge::getName)
                .collect(Collectors.toList());
    }
}
