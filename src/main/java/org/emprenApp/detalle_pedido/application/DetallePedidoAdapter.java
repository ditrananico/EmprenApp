package org.emprenApp.detalle_pedido.application;

import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoAddRequest;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.shared.application.exception.BaseException;

import java.util.List;

public interface DetallePedidoAdapter {
    List<DetallePedidoResponse> obtenerDetallesPorPedido(Long pedidoId) throws BaseException;
    DetallePedidoResponse agregarDetalle(DetallePedidoAddRequest request) throws BaseException;
    void eliminarDetalle(Long id) throws BaseException;
}
