package org.emprenApp.detalle_pedido.infrastructure.mapper;

import javax.annotation.processing.Generated;
import org.emprenApp.detalle_pedido.domain.DetallePedido;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-13T18:32:12-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
public class DetallePedidoInfrastructureMapperImpl implements DetallePedidoInfrastructureMapper {

    @Override
    public DetallePedidoResponse toResponse(DetallePedido detallePedido) {
        if ( detallePedido == null ) {
            return null;
        }

        DetallePedidoResponse.DetallePedidoResponseBuilder detallePedidoResponse = DetallePedidoResponse.builder();

        detallePedidoResponse.id( detallePedido.getId() );
        detallePedidoResponse.precioUnitario( detallePedido.getPrecioUnitario() );
        detallePedidoResponse.cantidad( detallePedido.getCantidad() );

        return detallePedidoResponse.build();
    }
}
