package org.emprenApp.auth.infrastructure.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor @NoArgsConstructor
public class LoginRequest {

    private String email;
    @ToString.Exclude
    private String password;

}
