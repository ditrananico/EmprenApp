package org.emprenApp.role.infrastructure.controller;

import org.emprenApp.role.application.RoleAdapter;
import org.emprenApp.role.application.dto.RoleDTO;
import org.emprenApp.role.infrastructure.mapper.RoleInfrastructureMapper;
import org.emprenApp.role.infrastructure.request.RoleCreateRequest;
import org.emprenApp.role.infrastructure.request.RoleUpdateRequest;
import org.emprenApp.role.infrastructure.response.RoleResponse;
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
@RequestMapping("v1/role")
public class RoleController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleAdapter roleAdapter;

    @PostMapping("/")
    public ResponseEntity<RoleResponse> createRole(@RequestBody @Validated RoleCreateRequest request) throws BaseException {
        logger.info("Creating role: {}", request.getName());
        RoleDTO created = roleAdapter.createRole(request);
        return responseCreated(RoleInfrastructureMapper.INSTANCE.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable Long id) throws BaseException {
        RoleDTO dto = roleAdapter.getRoleById(id);
        return responseOk(RoleInfrastructureMapper.INSTANCE.toResponse(dto));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<RoleResponse>> getActiveRoles(Pageable pageable) throws BaseException {
        Page<RoleDTO> page = roleAdapter.getActiveRoles(pageable);
        return responseOk(page.map(RoleInfrastructureMapper.INSTANCE::toResponse));
    }

    @PutMapping("/")
    public ResponseEntity<RoleResponse> updateRole(@RequestBody @Validated RoleUpdateRequest request) throws BaseException {
        logger.info("Updating role: {}", request.getId());
        RoleDTO updated = roleAdapter.updateRole(request);
        return responseOk(RoleInfrastructureMapper.INSTANCE.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteRole(@PathVariable Long id) throws BaseException {
        logger.info("Deleting role: {}", id);
        return responseOk(roleAdapter.deleteRole(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<RoleResponse> activateRole(@PathVariable Long id) throws BaseException {
        logger.info("Activating role: {}", id);
        RoleDTO activated = roleAdapter.activateRole(id);
        return responseOk(RoleInfrastructureMapper.INSTANCE.toResponse(activated));
    }
}
