package org.emprenApp.localidad.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalidadDTO {
    private Long id;
    private String provincia;
    private String partido;
    private String localidad;
    private String codigoPostal;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Boolean active;
}
