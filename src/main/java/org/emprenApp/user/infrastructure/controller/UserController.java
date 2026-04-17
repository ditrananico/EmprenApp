package org.emprenApp.user.infrastructure.controller;

import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("v1/user")

public class UserController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserAdapter userAdapter;

    @GetMapping("/all")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) throws BaseException {
        return ResponseEntity.ok(userAdapter.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    public UserResponse getUserByID(@PathVariable Long id) throws BaseException {
        UserDTO userDTO = userAdapter.getUserByID(id);
        return UserInfrastructureMapper.INSTANCE.toResponse(userDTO);
    }

    @GetMapping("/{email}")
    public UserResponse getUserByEmail(@PathVariable String email) throws BaseException {
        UserDTO userDTO = userAdapter.getUser(email);
        return UserInfrastructureMapper.INSTANCE.toResponse(userDTO);
    }

    @PostMapping("/create")
    public ResponseEntity createUser(@RequestBody @Validated UserCreateRequest userCreateRequest) throws BaseException {
        try {
            logger.info("Creando usuario: " + userCreateRequest.toString());
            return responseOk(userAdapter.createUser(userCreateRequest));
        } catch (GenericException e) {
            logger.error("Error al crear usuario: ", e);
            return responseError(e);
        }
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) throws BaseException {
             userAdapter.deleteUser(email);
        return ResponseEntity.ok("Usuario con email " + email + " eliminado correctamente");
    }


    @PutMapping("/edit")
    public ResponseEntity<UserResponse> updateUser(@RequestBody @Validated UserUpdateRequest updateRequest) throws BaseException {
        try {
            logger.info("Actualizando usuario: " + updateRequest.getEmail());

            UserDTO updatedUser = userAdapter.updateUser(updateRequest);

            UserResponse response = UserInfrastructureMapper.INSTANCE.toResponse(updatedUser);
            return ResponseEntity.ok(response);
        } catch (GenericException e) {
            logger.error("Error al actualizar usuario: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/edit/status/{id}")
    public ResponseEntity<String> updateStatusUser(@PathVariable Long id) throws BaseException {
        try {
            logger.info("Actualizando estado usuario: " + id);

            userAdapter.updateStatusUser(id);

            return ResponseEntity.ok("Estado del usuario actualizado exitosamente");
        } catch (GenericException e) {
            logger.error("Error al actualizar usuario: ", e);
            return responseError(e);
        }
    }


    private ResponseEntity<UserDTO> responseOk(UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    private ResponseEntity<String> responseError(GenericException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mensajeError(e));
    }

    private String mensajeError(GenericException e) {
        return e.getError();
    }
}
