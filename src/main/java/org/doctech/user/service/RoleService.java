package org.doctech.user.service;

import org.doctech.user.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleService {
    Role createRole(Role role);

    Role updateRole(UUID id, Role role);

    void deleteRole(UUID id);

    Optional<Role> getRoleById(UUID id);

    Optional<Role> getRoleByName(String name);

    List<Role> getAllRoles();

    Page<Role> getAllRolesPaged(Pageable pageable);

    boolean existsByName(String name);
}