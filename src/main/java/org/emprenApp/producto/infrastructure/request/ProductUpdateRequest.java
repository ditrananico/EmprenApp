package org.emprenApp.producto.infrastructure.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    @NotNull
    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Integer stockMinimo;
    private Long categoriaId;
}
