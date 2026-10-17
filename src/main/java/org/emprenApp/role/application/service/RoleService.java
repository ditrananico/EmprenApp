package org.emprenApp.role.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.permission.domain.Permission;
import org.emprenApp.permission.domain.PermissionRepository;
import org.emprenApp.role.application.RoleAdapter;
import org.emprenApp.role.application.dto.RoleDTO;
import org.emprenApp.role.application.mapper.RoleMapper;
import org.emprenApp.role.domain.Role;
import org.emprenApp.role.domain.RoleRepository;
import org.emprenApp.role.infrastructure.request.RoleCreateRequest;
import org.emprenApp.role.infrastructure.request.RoleUpdateRequest;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService implements RoleAdapter {

    private final static Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleValidationService roleValidationService;

    @Override
    @Transactional
    public RoleDTO createRole(RoleCreateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ValidateGeneric.validateNotBlank(request.getName());
            roleValidationService.validateFields(request.getName(), request.getDescription());

            String name = request.getName().trim();
            if (roleRepository.existsByNameIgnoreCaseAndActiveTrue(name)) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }

            Role role = RoleMapper.INSTANCE.toEntity(request);
            role.setName(name);
            if (role.getDescription() != null) {
                role.setDescription(role.getDescription().trim());
            }
            role.setPermissions(findPermissions(request.getPermissionIds()));
            Role created = roleRepository.save(role);
            logger.info("Role created: {}", created.getId());
            return RoleMapper.INSTANCE.toDTO(created);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating role", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return RoleMapper.INSTANCE.toDTO(roleRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> getActiveRoles(Pageable pageable) throws BaseException {
        try {
            return roleRepository.findAllByActiveTrue(pageable).map(RoleMapper.INSTANCE::toDTO);
        } catch (Exception e) {
            logger.error("Error listing active roles", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public RoleDTO updateRole(RoleUpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ValidateGeneric.validateId(request.getId());

            Role role = roleRepository.findByIdAndActiveTrue(request.getId()).orElseThrow(NotFoundException::new);

            roleValidationService.validateFields(request.getName(), request.getDescription());

            if (request.getName() != null) {
                String name = request.getName().trim();
                if (roleRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot(name, role.getId())) {
                    throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
                }
            }
            RoleMapper.INSTANCE.updateEntityFromRequest(request, role);
            if (request.getName() != null) {
                role.setName(request.getName().trim());
            }
            if (request.getDescription() != null) {
                role.setDescription(request.getDescription().trim());
            }
            if (request.getPermissionIds() != null) {
                role.setPermissions(findPermissions(request.getPermissionIds()));
            }

            Role updated = roleRepository.save(role);
            logger.info("Role updated: {}", updated.getId());
            return RoleMapper.INSTANCE.toDTO(updated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating role", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public RoleDTO activateRole(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            Role role = roleRepository.findById(id).orElseThrow(NotFoundException::new);
            if (Boolean.TRUE.equals(role.getActive())) {
                return RoleMapper.INSTANCE.toDTO(role);
            }
            if (roleRepository.existsByNameIgnoreCaseAndActiveTrue(role.getName())) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }
            role.setActive(true);
            Role activated = roleRepository.save(role);
            logger.info("Role reactivated ID: {}", id);
            return RoleMapper.INSTANCE.toDTO(activated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error activating role ID: {}", id, e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public boolean deleteRole(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            Role role = roleRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new);
            role.setActive(false);
            roleRepository.save(role);
            logger.info("Role deactivated (soft delete) ID: {}", id);
            return true;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting role ID: {}", id, e);
            throw new GenericException();
        }
    }

    private Set<Permission> findPermissions(List<Long> permissionIds) throws BaseException {
        Set<Permission> permissions = new HashSet<>();
        if (permissionIds == null) {
            return permissions;
        }
        for (Long permissionId : permissionIds) {
            ValidateGeneric.validateId(permissionId);
            permissions.add(permissionRepository.findByIdAndActiveTrue(permissionId)
                    .orElseThrow(() -> new BaseException(ErrorCodeEnum.INVALID_PARAMETERS)));
        }
        return permissions;
    }
}
