package org.emprenApp.detalle_pedido.application.mapper;

import org.emprenApp.detalle_pedido.domain.DetallePedido;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.detalle_pedido.infrastructure.response.DetalleResponseItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DetallePedidoMapper {
    DetallePedidoMapper INSTANCE = Mappers.getMapper(DetallePedidoMapper.class);

    default DetallePedidoResponse toResponse(List<DetallePedido> detallesGuardados) {
        DetallePedidoResponse response = new DetallePedidoResponse();
        response.setPedidoId(extraerPedidoId(detallesGuardados));
        response.setTotalPedido(calcularTotalPedido(detallesGuardados));
        response.setItems(toItemResponseList(detallesGuardados));
        return response;
    }


    @Mapping(target = "detallePedidoId", source = "id")
    @Mapping(target = "productoId", source = "productoId.id")
    @Mapping(target = "subtotal", expression = "java(calcularSubtotal(detalle))")
    DetalleResponseItem toItemResponse(DetallePedido detalle);

    List<DetalleResponseItem> toItemResponseList(List<DetallePedido> detalles);

    default BigDecimal calcularSubtotal(DetallePedido detalle) {
        if (detalle == null || detalle.getPrecioUnitario() == null || detalle.getCantidad() == null) {
            return BigDecimal.ZERO;
        }
        return detalle.getPrecioUnitario().multiply(new BigDecimal(detalle.getCantidad()));
    }


    default Long extraerPedidoId(List<DetallePedido> detalles) {
        if (detalles == null || detalles.isEmpty() || detalles.get(0).getPedidoId() == null) {
            return null;
        }
        return detalles.get(0).getPedidoId().getId();
    }

    default BigDecimal calcularTotalPedido(List<DetallePedido> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido d : detalles) {
            if (d.getPrecioUnitario() != null && d.getCantidad() != null) {
                BigDecimal subtotalRenglon = d.getPrecioUnitario().multiply(new BigDecimal(d.getCantidad()));
                total = total.add(subtotalRenglon);
            }
        }
        return total;
    }

}
