package org.emprenApp.detalle_pedido.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetallePedidoDTO {
    private Long id;
    private Long pedidoId;
    private List<Long> productoIds;
    private List<BigDecimal> precioUnitario;
    private List<Integer> cantidad;
    private BigDecimal total;
}
