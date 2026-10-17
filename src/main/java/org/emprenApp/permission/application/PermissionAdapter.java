package org.emprenApp.permission.application;

import org.emprenApp.permission.application.dto.PermissionDTO;
import org.emprenApp.permission.infrastructure.request.PermissionCreateRequest;
import org.emprenApp.permission.infrastructure.request.PermissionUpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionAdapter {

    PermissionDTO createPermission(PermissionCreateRequest request) throws BaseException;

    PermissionDTO getPermissionById(Long id) throws BaseException;

    Page<PermissionDTO> getActivePermissions(Pageable pageable) throws BaseException;

    PermissionDTO updatePermission(PermissionUpdateRequest request) throws BaseException;

    boolean deletePermission(Long id) throws BaseException;

    PermissionDTO activatePermission(Long id) throws BaseException;
}
