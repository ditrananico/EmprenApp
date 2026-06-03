package org.emprenApp.detalle_pedido.infrastructure.request;

import lombok.Data;

import java.util.List;


@Data
public class DetallePedidoAddRequest {

    private Long pedidoId;
    private List<DetallePedidoRequestItem> itemsDetallePedido;
}
