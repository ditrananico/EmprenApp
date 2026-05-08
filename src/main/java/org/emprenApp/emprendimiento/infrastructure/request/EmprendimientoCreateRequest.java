package org.emprenApp.emprendimiento.infrastructure.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class EmprendimientoCreateRequest {
    @NotBlank(message = "El nombre no puede estar vacio")
    @Size(max = 40, message = "El nombre no puede tener mas de 40 caracteres")
    private String name;
    @NotBlank(message = "La descripcion no puede estar vacia")
    @Size(max = 250, message = "La descripcion no puede tener mas de 250 caracteres")
    private String description;
    // Falta ubicación y usuario id
}