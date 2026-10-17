package org.emprenApp.user.infrastructure.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "El email no puede estar vacío")
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
}
