package org.emprenApp.user.infrastructure.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor @NoArgsConstructor
public class UserCreateRequest {

    private String email;
    @ToString.Exclude
    private String password;
    private String firstName;
    private String lastName;
    private String phone;

}
