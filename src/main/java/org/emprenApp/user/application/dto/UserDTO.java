package org.emprenApp.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.shared.application.enums.EstadoUserEnum;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String email;
    private String password;
    private Timestamp fechaCreacion;
    private EstadoUserEnum estado;
    private String nombre;
    private String apellido;
    private String telefono;
}
