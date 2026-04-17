package org.emprenApp.producto.infrastructure.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    @NotNull
    private String titulo;
    private String descripcion;
    @NotNull
    private BigDecimal precio;
    private Integer stock;
    private Integer stockMinimo;
    @NotNull
    private Long categoriaId;
    @NotNull
    private Long emprendimientoId;
}
