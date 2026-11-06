package org.emprenApp.auth.application;

import org.emprenApp.auth.application.dto.TokenDTO;
import org.emprenApp.auth.infrastructure.request.LoginRequest;
import org.emprenApp.auth.infrastructure.request.RegisterRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.user.application.dto.UserDTO;

public interface AuthAdapter {

    UserDTO register(RegisterRequest registerRequest) throws BaseException;
    TokenDTO login(LoginRequest loginRequest) throws BaseException;

}
