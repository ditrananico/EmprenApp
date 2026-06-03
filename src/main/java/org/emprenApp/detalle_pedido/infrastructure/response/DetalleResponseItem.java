package org.emprenApp.detalle_pedido.infrastructure.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleResponseItem {
    private Long detallePedidoId;
    private Long productoId;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal subtotal;
}
