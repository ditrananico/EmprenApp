package org.emprenApp.detalle_pedido.application.mapper;

import org.emprenApp.detalle_pedido.domain.DetallePedido;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DetallePedidoMapper {
    DetallePedidoMapper INSTANCE = Mappers.getMapper(DetallePedidoMapper.class);

    @Mapping(target = "subtotal", expression = "java(calcularSubtotal(detallePedido))")
    List<DetallePedidoResponse> toListResponse(List<DetallePedido> detallePedidos);

    @Mapping(target = "subtotal", expression = "java(calcularSubtotal(detallePedido))")
    DetallePedidoResponse toResponse(DetallePedido detallePedido);

    default BigDecimal calcularSubtotal(DetallePedido d) {
        if (d.getPrecioUnitario() == null || d.getCantidad() == null) {
            return BigDecimal.ZERO;
        }
        return d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad()));
    }

}
