package org.emprenApp.detalle_pedido.infrastructure.mapper;

import org.emprenApp.detalle_pedido.domain.DetallePedido;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DetallePedidoInfrastructureMapper {
    DetallePedidoInfrastructureMapper INSTANCE = Mappers.getMapper(DetallePedidoInfrastructureMapper.class);

    DetallePedidoResponse toResponse(DetallePedido detallePedido);
}
