package org.emprenApp.detalle_pedido.infrastructure.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DetallePedidoResponse {
    private Long pedidoId;
    private List<DetalleResponseItem> items;
    private BigDecimal totalPedido;
}
