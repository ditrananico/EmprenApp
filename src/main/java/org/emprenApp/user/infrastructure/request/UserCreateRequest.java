package org.emprenApp.user.infrastructure.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class UserCreateRequest {

    private String email;
    private String password;
    private String nombre;
    private String apellido;
    private String telefonoPersonal;

}
