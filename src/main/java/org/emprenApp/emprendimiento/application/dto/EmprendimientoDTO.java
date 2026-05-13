package org.emprenApp.emprendimiento.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.shared.application.enums.EstadoEmprendimientoEnum;

@Data
@AllArgsConstructor @NoArgsConstructor
public class EmprendimientoDTO {

    private Long id;
    private String name;
    private String description;
    private EstadoEmprendimientoEnum estado;
}