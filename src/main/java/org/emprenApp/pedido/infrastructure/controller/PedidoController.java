package org.emprenApp.pedido.infrastructure.controller;

import org.emprenApp.pedido.application.PedidoAdapter;
import org.emprenApp.pedido.application.dto.PedidoDTO;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.EstadoPedidoEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/pedido")
public class PedidoController extends BaseRestController {
    private final static Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    private PedidoAdapter pedidoAdapter;

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> getPedidoByID(@PathVariable Long id) throws BaseException {
        return responseOk(pedidoAdapter.getPedidoByID(id));
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<Page<PedidoDTO>> getAllPedidoByIDUserAndStatus(@PathVariable Long userId, @RequestParam(required = false) EstadoPedidoEnum status, Pageable pageable) throws BaseException {
        return responseOk(pedidoAdapter.getAllPedidoByUserIDAndStatus(userId, status, pageable));
    }

    @GetMapping("/emprendimiento/{emprendimientoId}")
    public ResponseEntity<Page<PedidoDTO>> getPedidosByEmprendimiento(@PathVariable Long emprendimientoId, @RequestParam(required = false) EstadoPedidoEnum status, Pageable pageable) throws BaseException {
        return responseOk(pedidoAdapter.getAllPedidoByEmprendimientoIDAndStatus(emprendimientoId, status, pageable));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<PedidoDTO> updateStatus(@PathVariable Long id, @RequestParam EstadoPedidoEnum nuevoEstado) throws BaseException {
        logger.info("Actualizando estado del pedido: " + id + " a " + nuevoEstado);
        return responseOk(pedidoAdapter.updateStatus(id, nuevoEstado));
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<String> cancelPedido(@PathVariable Long id) throws BaseException {

        logger.info("REST Request - DELETE /cancel/{} - Solicitud de cancelación", id);
        boolean fueCancelado = pedidoAdapter.cancelPedido(id);
        if (fueCancelado) {
            logger.info("REST Response - Pedido {} cancelado con éxito", id);
            return ResponseEntity.ok("Pedido cancelado exitosamente");
        } else {
            logger.info("REST Response - El pedido {} no requirió cambios (ya estaba cancelado)", id);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorCodeEnum.PEDIDO_ERROR_CANCELAR.getDescription());
        }
    }
}
