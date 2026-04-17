package org.emprenApp.pedido.application;

import org.emprenApp.pedido.application.dto.PedidoDTO;
import org.emprenApp.shared.application.enums.EstadoPedidoEnum;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PedidoAdapter {

    Page<PedidoDTO> getAllPedidos(Pageable pageable) throws GenericException;
    PedidoDTO getPedidoByID(Long id) throws GenericException, NotFoundException;
    Page<PedidoDTO> getAllPedidoByUserIDAndStatus(Long userId, EstadoPedidoEnum status, Pageable pageable) throws GenericException, NotFoundException;
    Page<PedidoDTO> getAllPedidoByEmprendimientoIDAndStatus(Long emprendimientoId, EstadoPedidoEnum status, Pageable pageable) throws GenericException, NotFoundException;

    PedidoDTO updateStatus(Long id, EstadoPedidoEnum nuevoEstado) throws GenericException, NotFoundException;
    String cancelPedido(Long id) throws GenericException, NotFoundException;

}
