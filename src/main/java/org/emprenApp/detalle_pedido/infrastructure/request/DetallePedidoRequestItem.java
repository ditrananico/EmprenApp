package org.emprenApp.detalle_pedido.infrastructure.request;

import lombok.Data;

import java.util.List;

@Data
public class DetallePedidoRequestItem {
    private Long productoId;
    private Integer cantidad;
}
