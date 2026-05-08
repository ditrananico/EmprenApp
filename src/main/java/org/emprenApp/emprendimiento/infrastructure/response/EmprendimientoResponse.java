package org.emprenApp.emprendimiento.infrastructure.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class EmprendimientoResponse {

    private Long id;
    private String name;
    private String description;
    // falta ubicación y usuario id
}
