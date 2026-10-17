package org.emprenApp.role.application;

import org.emprenApp.role.application.dto.RoleDTO;
import org.emprenApp.role.infrastructure.request.RoleCreateRequest;
import org.emprenApp.role.infrastructure.request.RoleUpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleAdapter {

    RoleDTO createRole(RoleCreateRequest request) throws BaseException;

    RoleDTO getRoleById(Long id) throws BaseException;

    Page<RoleDTO> getActiveRoles(Pageable pageable) throws BaseException;

    RoleDTO updateRole(RoleUpdateRequest request) throws BaseException;

    boolean deleteRole(Long id) throws BaseException;

    RoleDTO activateRole(Long id) throws BaseException;
}
