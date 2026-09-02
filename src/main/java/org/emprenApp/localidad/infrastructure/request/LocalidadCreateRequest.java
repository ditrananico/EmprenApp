package org.emprenApp.localidad.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocalidadCreateRequest {

    @NotNull
    private String provincia;

    @NotNull
    private String partido;

    @NotNull
    private String localidad;

    private String codigoPostal;

    private BigDecimal latitud;

    private BigDecimal longitud;
}
