package org.emprenApp.user.infrastructure.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class UserCreateRequest {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;

}
