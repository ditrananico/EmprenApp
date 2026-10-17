package org.emprenApp.permission.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.permission.application.PermissionAdapter;
import org.emprenApp.permission.application.dto.PermissionDTO;
import org.emprenApp.permission.application.mapper.PermissionMapper;
import org.emprenApp.permission.domain.Permission;
import org.emprenApp.permission.domain.PermissionRepository;
import org.emprenApp.permission.infrastructure.request.PermissionCreateRequest;
import org.emprenApp.permission.infrastructure.request.PermissionUpdateRequest;
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

@Service
@RequiredArgsConstructor
public class PermissionService implements PermissionAdapter {

    private final static Logger logger = LoggerFactory.getLogger(PermissionService.class);

    private final PermissionRepository permissionRepository;
    private final PermissionValidationService permissionValidationService;

    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionCreateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ValidateGeneric.validateNotBlank(request.getName());
            permissionValidationService.validateFields(request.getName(), request.getDescription());

            String name = request.getName().trim();
            if (permissionRepository.existsByNameIgnoreCaseAndActiveTrue(name)) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }

            Permission permission = PermissionMapper.INSTANCE.toEntity(request);
            permission.setName(name);
            if (permission.getDescription() != null) {
                permission.setDescription(permission.getDescription().trim());
            }
            Permission created = permissionRepository.save(permission);
            logger.info("Permission created: {}", created.getId());
            return PermissionMapper.INSTANCE.toDTO(created);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating permission", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionDTO getPermissionById(Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return PermissionMapper.INSTANCE.toDTO(permissionRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionDTO> getActivePermissions(Pageable pageable) throws BaseException {
        try {
            return permissionRepository.findAllByActiveTrue(pageable).map(PermissionMapper.INSTANCE::toDTO);
        } catch (Exception e) {
            logger.error("Error listing active permissions", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public PermissionDTO updatePermission(PermissionUpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ValidateGeneric.validateId(request.getId());

            Permission permission = permissionRepository.findByIdAndActiveTrue(request.getId()).orElseThrow(NotFoundException::new);

            permissionValidationService.validateFields(request.getName(), request.getDescription());

            if (request.getName() != null) {
                String name = request.getName().trim();
                if (permissionRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot(name, permission.getId())) {
                    throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
                }
            }
            PermissionMapper.INSTANCE.updateEntityFromRequest(request, permission);
            if (request.getName() != null) {
                permission.setName(request.getName().trim());
            }
            if (request.getDescription() != null) {
                permission.setDescription(request.getDescription().trim());
            }

            Permission updated = permissionRepository.save(permission);
            logger.info("Permission updated: {}", updated.getId());
            return PermissionMapper.INSTANCE.toDTO(updated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating permission", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public PermissionDTO activatePermission(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            Permission permission = permissionRepository.findById(id).orElseThrow(NotFoundException::new);
            if (Boolean.TRUE.equals(permission.getActive())) {
                return PermissionMapper.INSTANCE.toDTO(permission);
            }
            if (permissionRepository.existsByNameIgnoreCaseAndActiveTrue(permission.getName())) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }
            permission.setActive(true);
            Permission activated = permissionRepository.save(permission);
            logger.info("Permission reactivated ID: {}", id);
            return PermissionMapper.INSTANCE.toDTO(activated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error activating permission ID: {}", id, e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public boolean deletePermission(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            Permission permission = permissionRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new);
            permission.setActive(false);
            permissionRepository.save(permission);
            logger.info("Permission deactivated (soft delete) ID: {}", id);
            return true;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting permission ID: {}", id, e);
            throw new GenericException();
        }
    }
}
