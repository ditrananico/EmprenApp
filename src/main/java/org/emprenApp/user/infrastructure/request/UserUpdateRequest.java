package org.emprenApp.user.infrastructure.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Un campo null no se modifica. El email no se cambia por aca (requiere re-verificacion).
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    private String firstName;
    private String lastName;
    private String phone;
}
