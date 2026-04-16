package org.emprenApp.detalle_pedido.infrastructure.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DetallePedidoResponse {
    private Long id;
    private Long pedidoId;
    private Long productoId;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}
