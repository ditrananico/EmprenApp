package org.emprenApp.publicacion.infrastructure.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicacionCreateRequest {

    @NotBlank(message = "El titulo no puede estar vacio")
    @Size(max = 100, message = "El titulo no puede tener mas de 100 caracteres")
    private String titulo;

    @NotBlank(message = "La descripcion no puede estar vacia")
    @Size(max = 250, message = "La descripcion no puede tener mas de 250 caracteres")
    private String descripcion;

    // Falta emprendimiento_id
}
