package org.emprenApp.localidad.infrastructure.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalidadResponse {
    private Long id;
    private String provincia;
    private String partido;
    private String localidad;
    private String codigoPostal;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Boolean active;
}
