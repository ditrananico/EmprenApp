package org.emprenApp.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.auth.application.AuthAdapter;
import org.emprenApp.auth.application.dto.TokenDTO;
import org.emprenApp.auth.infrastructure.request.LoginRequest;
import org.emprenApp.auth.infrastructure.request.RegisterRequest;
import org.emprenApp.auth.infrastructure.security.JwtService;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.user.application.UserAdapter;
import org.emprenApp.user.application.dto.UserDTO;
import org.emprenApp.user.infrastructure.request.UserCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthAdapter {
    private final static Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final String TOKEN_TYPE = "Bearer";

    private final UserAdapter userAdapter;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Delega en createUser: validaciones, rol por defecto (END_USER) y hash de la clave viven en un solo lugar.
    // No devuelve token: el usuario se registra y despues hace login.
    @Override
    public UserDTO register(RegisterRequest registerRequest) throws BaseException {
        ValidateGeneric.validateNotNull(registerRequest);
        return userAdapter.createUser(new UserCreateRequest(registerRequest.getEmail(), registerRequest.getPassword(),
                registerRequest.getFirstName(), registerRequest.getLastName(), registerRequest.getPhone()));
    }

    @Override
    public TokenDTO login(LoginRequest loginRequest) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(loginRequest);
            ValidateGeneric.validateNotBlank(loginRequest.getEmail());
            ValidateGeneric.validateNotBlank(loginRequest.getPassword());

            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getEmail(), loginRequest.getPassword()));

            String token = jwtService.generateToken(authentication.getName());
            logger.info("Login correcto: {}", loginRequest.getEmail());
            return new TokenDTO(token, TOKEN_TYPE, jwtService.getExpirationSeconds());
        } catch (BaseException e) {
            throw e;
        } catch (AuthenticationException e) {
            // Clave incorrecta, email inexistente o usuario deshabilitado: misma respuesta para no revelar cual fue.
            logger.warn("Login rechazado: {} ({})", loginRequest.getEmail(), e.getClass().getSimpleName());
            throw new BaseException(ErrorCodeEnum.INVALID_CREDENTIALS);
        } catch (Exception e) {
            logger.error("Error inesperado en el login:", e);
            throw new GenericException();
        }
    }
}
