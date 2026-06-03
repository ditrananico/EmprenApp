package org.emprenApp.publicacion.infrastructure.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicacionResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private Timestamp fechaCreacion;
    private Integer boost;
}
