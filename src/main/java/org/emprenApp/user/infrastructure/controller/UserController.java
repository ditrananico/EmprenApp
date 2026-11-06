package org.emprenApp.user.infrastructure.controller;

import org.emprenApp.auth.infrastructure.security.AuthUser;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.user.application.UserAdapter;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.mapper.UserInfrastructureMapper;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.request.UserUpdateRequest;
import org.emprenApp.user.infrastructure.response.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("v1/user")
public class UserController extends BaseRestController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserAdapter userAdapter;

    @PostMapping("/")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Validated UserCreateRequest userCreateRequest) throws BaseException {
        logger.info("Creando usuario: {}", userCreateRequest.getEmail());
        UserDTO created = userAdapter.createUser(userCreateRequest);
        return responseCreated(UserInfrastructureMapper.INSTANCE.toResponse(created));
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal AuthUser authUser) throws BaseException {
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(userAdapter.getUserByID(authUser.getId())));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) throws BaseException {
        Page<UserDTO> page = userAdapter.getAllUsers(pageable);
        return responseOk(page.map(UserInfrastructureMapper.INSTANCE::toResponse));
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) throws BaseException {
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(userAdapter.getUser(email)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserByID(@PathVariable Long id) throws BaseException {
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(userAdapter.getUserByID(id)));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@AuthenticationPrincipal AuthUser authUser,
                                                        @RequestBody @Validated UserUpdateRequest request) throws BaseException {
        logger.info("Actualizando mi perfil ID: {}", authUser.getId());
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(userAdapter.updateUser(authUser.getId(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @RequestBody @Validated UserUpdateRequest request) throws BaseException {
        logger.info("Actualizando usuario ID: {}", id);
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(userAdapter.updateUser(id, request)));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Boolean> deleteMyUser(@AuthenticationPrincipal AuthUser authUser) throws BaseException {
        logger.info("Dando de baja mi usuario ID: {}", authUser.getId());
        return responseOk(userAdapter.deleteUser(authUser.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable Long id) throws BaseException {
        logger.info("Dando de baja usuario ID: {}", id);
        return responseOk(userAdapter.deleteUser(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) throws BaseException {
        logger.info("Reactivando usuario ID: {}", id);
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(userAdapter.activateUser(id)));
    }

}
