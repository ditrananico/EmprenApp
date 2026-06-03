package org.emprenApp.publicacion.infrastructure.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicacionUpdateRequest {

    @Size(max = 100, message = "El titulo no puede tener mas de 100 caracteres")
    private String titulo;

    @Size(max = 250, message = "La descripcion no puede tener mas de 250 caracteres")
    private String descripcion;
}
