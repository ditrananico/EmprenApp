package org.emprenApp.detalle_pedido.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetallePedidoDTO {
    private Long id;
    private Long pedidoId;
    private Long productoId;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal; // Campo calculado
}
