package org.emprenApp.publicacion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor @NoArgsConstructor
public class PublicacionDTO {

    private Long id;
    private String titulo;
    private String descripcion;
    private Timestamp fechaCreacion;
    private Integer boost;
    //todo agregar estado
}
