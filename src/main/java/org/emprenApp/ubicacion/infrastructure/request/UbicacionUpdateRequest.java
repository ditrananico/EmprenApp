package org.emprenApp.ubicacion.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UbicacionUpdateRequest {

    @NotNull
    private Long id;

    private String calle;

    private Integer numero;

    private String piso;

    private String depto;

    private String referencia;

    private Long localidadId;

    private String alias;

    private Boolean predeterminada;
}
