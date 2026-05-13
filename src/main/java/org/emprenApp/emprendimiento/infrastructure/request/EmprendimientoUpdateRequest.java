package org.emprenApp.emprendimiento.infrastructure.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class EmprendimientoUpdateRequest {
    @Size(max = 40, message = "El nombre no puede tener mas de 40 caracteres")
    private String name;

    @Size(max = 250, message = "La descripcion no puede tener mas de 250 caracteres")
    private String description;
}