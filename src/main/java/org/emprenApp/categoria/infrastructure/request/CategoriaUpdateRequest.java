package org.emprenApp.categoria.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoriaUpdateRequest {

    @NotNull
    private Long id;

    private String nombre;

    private String descripcion;

    private String tipo;
}
