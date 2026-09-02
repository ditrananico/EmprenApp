package org.emprenApp.ubicacion.infrastructure.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.localidad.infrastructure.response.LocalidadResponse;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UbicacionResponse {
    private Long id;
    private String calle;
    private Integer numero;
    private String piso;
    private String depto;
    private String referencia;
    private LocalidadResponse localidad;
    private Long usuarioId;
    private String alias;
    private Boolean predeterminada;
    private Boolean activo;
}
