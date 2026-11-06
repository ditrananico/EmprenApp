package org.emprenApp.auth.infrastructure.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType;
    // segundos hasta que el token vence.
    private Long expiresIn;

}
