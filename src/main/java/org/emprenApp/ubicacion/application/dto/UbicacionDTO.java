package org.emprenApp.ubicacion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.localidad.application.dto.LocalidadDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UbicacionDTO {
    private Long id;
    private String calle;
    private Integer numero;
    private String piso;
    private String depto;
    private String referencia;
    private LocalidadDTO localidad;
    private Long usuarioId;
    private String alias;
    private Boolean predeterminada;
    private Boolean activo;
}
