package org.emprenApp.detalle_pedido.application;

import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoAddRequest;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;

import java.util.List;

public interface DetallePedidoAdapter {
    List<DetallePedidoResponse> obtenerDetallesPorPedido(Long pedidoId) throws GenericException;
    DetallePedidoResponse agregarDetalle(DetallePedidoAddRequest request) throws GenericException, NotFoundException;
    void eliminarDetalle(Long id) throws GenericException, NotFoundException;
}
