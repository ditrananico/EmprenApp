package org.emprenApp.auth.infrastructure.controller;

import org.emprenApp.auth.application.AuthAdapter;
import org.emprenApp.auth.infrastructure.mapper.AuthInfrastructureMapper;
import org.emprenApp.auth.infrastructure.request.LoginRequest;
import org.emprenApp.auth.infrastructure.request.RegisterRequest;
import org.emprenApp.auth.infrastructure.response.LoginResponse;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.user.infrastructure.mapper.UserInfrastructureMapper;
import org.emprenApp.user.infrastructure.response.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Rutas publicas (ver SecurityConfig.PUBLIC_PATHS).
@RestController
@RequestMapping("v1/auth")
public class AuthController extends BaseRestController {
    private final static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthAdapter authAdapter;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Validated RegisterRequest request) throws BaseException {
        logger.info("Registrando usuario: {}", request.getEmail());
        return responseCreated(UserInfrastructureMapper.INSTANCE.toResponse(authAdapter.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Validated LoginRequest request) throws BaseException {
        return responseOk(AuthInfrastructureMapper.INSTANCE.toResponse(authAdapter.login(request)));
    }

}
