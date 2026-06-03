package org.emprenApp.detalle_pedido.application;

import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoAddRequest;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.shared.application.exception.BaseException;

public interface DetallePedidoAdapter {
    DetallePedidoResponse getDetallePedidoByPedidoId(Long pedidoId) throws BaseException;
    DetallePedidoResponse agregarDetallePedido(DetallePedidoAddRequest request) throws BaseException;
    void eliminarDetalle(Long id) throws BaseException;
}