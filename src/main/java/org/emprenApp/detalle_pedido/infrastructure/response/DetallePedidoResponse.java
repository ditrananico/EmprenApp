package org.emprenApp.detalle_pedido.infrastructure.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DetallePedidoResponse {
    private Long id;
    private Long pedidoId;
    private Long productoId;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal subtotal;
}
