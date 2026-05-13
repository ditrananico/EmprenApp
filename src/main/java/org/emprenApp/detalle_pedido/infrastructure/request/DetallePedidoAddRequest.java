package org.emprenApp.detalle_pedido.infrastructure.request;

import lombok.Data;

@Data
public class DetallePedidoAddRequest {
    private Long pedidoId;
    private Long productoId;
    private Integer cantidad;
}
