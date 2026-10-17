package org.emprenApp.permission.infrastructure.controller;

import org.emprenApp.permission.application.PermissionAdapter;
import org.emprenApp.permission.application.dto.PermissionDTO;
import org.emprenApp.permission.infrastructure.mapper.PermissionInfrastructureMapper;
import org.emprenApp.permission.infrastructure.request.PermissionCreateRequest;
import org.emprenApp.permission.infrastructure.request.PermissionUpdateRequest;
import org.emprenApp.permission.infrastructure.response.PermissionResponse;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/permission")
public class PermissionController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(PermissionController.class);

    @Autowired
    private PermissionAdapter permissionAdapter;

    @PostMapping("/")
    public ResponseEntity<PermissionResponse> createPermission(@RequestBody @Validated PermissionCreateRequest request) throws BaseException {
        logger.info("Creating permission: {}", request.getName());
        PermissionDTO created = permissionAdapter.createPermission(request);
        return responseCreated(PermissionInfrastructureMapper.INSTANCE.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermission(@PathVariable Long id) throws BaseException {
        PermissionDTO dto = permissionAdapter.getPermissionById(id);
        return responseOk(PermissionInfrastructureMapper.INSTANCE.toResponse(dto));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<PermissionResponse>> getActivePermissions(Pageable pageable) throws BaseException {
        Page<PermissionDTO> page = permissionAdapter.getActivePermissions(pageable);
        return responseOk(page.map(PermissionInfrastructureMapper.INSTANCE::toResponse));
    }

    @PutMapping("/")
    public ResponseEntity<PermissionResponse> updatePermission(@RequestBody @Validated PermissionUpdateRequest request) throws BaseException {
        logger.info("Updating permission: {}", request.getId());
        PermissionDTO updated = permissionAdapter.updatePermission(request);
        return responseOk(PermissionInfrastructureMapper.INSTANCE.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deletePermission(@PathVariable Long id) throws BaseException {
        logger.info("Deleting permission: {}", id);
        return responseOk(permissionAdapter.deletePermission(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PermissionResponse> activatePermission(@PathVariable Long id) throws BaseException {
        logger.info("Activating permission: {}", id);
        PermissionDTO activated = permissionAdapter.activatePermission(id);
        return responseOk(PermissionInfrastructureMapper.INSTANCE.toResponse(activated));
    }
}
