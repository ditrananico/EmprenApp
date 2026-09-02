package org.emprenApp.ubicacion.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UbicacionCreateRequest {

    @NotNull
    private String calle;

    @NotNull
    private Integer numero;

    private String piso;

    private String depto;

    private String referencia;

    @NotNull
    private Long localidadId;

    private Long usuarioId;

    private String alias;

    private Boolean predeterminada;
}
