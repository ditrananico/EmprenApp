package org.emprenApp.user.infrastructure.controller;

import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.user.application.UserAdapter;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.mapper.UserInfrastructureMapper;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.emprenApp.user.infrastructure.response.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/user")

public class UserController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    private UserAdapter userAdapter;

    @GetMapping("/{id}")
    public UserResponse getUserByUD(@PathVariable Long id) throws BaseException {
        UserDTO userDTO = userAdapter.getUserByID(id);

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
