package org.doctech.user.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.RoleAlreadyExistsException;
import org.doctech.common.exception.RoleNotFoundException;
import org.doctech.user.model.Role;
import org.doctech.user.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new RoleAlreadyExistsException("Role with name " + role.getName() + " already exists");
        }
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(UUID id, Role role) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + id));

        if (!existingRole.getName().equals(role.getName()) &&
                roleRepository.existsByName(role.getName())) {
            throw new RoleAlreadyExistsException("Role with name " + role.getName() + " already exists");
        }

        existingRole.setName(role.getName());
        return roleRepository.save(existingRole);
    }

    @Override
    public void deleteRole(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new RoleNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> getRoleById(UUID id) {
        return roleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Role> getAllRolesPaged(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}