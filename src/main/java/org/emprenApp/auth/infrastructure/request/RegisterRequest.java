package org.emprenApp.auth.infrastructure.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor @NoArgsConstructor
public class RegisterRequest {

    private String email;
    @ToString.Exclude
    private String password;
    private String firstName;
    private String lastName;
    private String phone;

}
