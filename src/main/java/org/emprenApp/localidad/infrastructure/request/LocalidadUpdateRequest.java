package org.emprenApp.localidad.infrastructure.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocalidadUpdateRequest {

    @NotNull
    private Long id;

    private String provincia;

    private String partido;

    private String localidad;

    private String codigoPostal;

    private BigDecimal latitud;

    private BigDecimal longitud;
}
