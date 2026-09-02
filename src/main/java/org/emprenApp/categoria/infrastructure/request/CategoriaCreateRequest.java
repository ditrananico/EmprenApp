package org.emprenApp.categoria.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoriaCreateRequest {

    @NotNull
    private String nombre;

    private String descripcion;

    private String tipo;
}
