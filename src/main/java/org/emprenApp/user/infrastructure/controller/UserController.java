package org.emprenApp.user.infrastructure.controller;

import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.application.ValidateGeneric;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("v1/user")

public class UserController extends BaseRestController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserAdapter userAdapter;

    @GetMapping("/all")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) throws BaseException {
        return responseOk(userAdapter.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserByID(@PathVariable Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(userAdapter.getUserByID(id)));
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) throws BaseException {
        UserDTO userDTO = userAdapter.getUser(email);
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(userDTO));
    }

    @PostMapping("/")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Validated UserCreateRequest userCreateRequest) throws BaseException {
            logger.info("Creando usuario: " + userCreateRequest.toString());
            return responseCreated(userAdapter.createUser(userCreateRequest));
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) throws BaseException {
        userAdapter.deleteUser(email);
        return responseOk(("usuario eliminado exitosamente"));
    }

    @PutMapping("/edit")
    public ResponseEntity<UserResponse> updateUser(@RequestBody @Validated UserUpdateRequest updateRequest) throws BaseException {
        logger.info("Actualizando usuario: " + updateRequest.getEmail());
        UserDTO updatedUser = userAdapter.updateUser(updateRequest);
        return responseOk(UserInfrastructureMapper.INSTANCE.toResponse(updatedUser));

    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<String> updateStatusUser(@PathVariable Long id) throws BaseException {
        logger.info("Actualizando estado usuario: " + id);
        userAdapter.updateStatusUser(id);
        return responseOk(("Estado del usuario actualizado exitosamente"));

    }

}
