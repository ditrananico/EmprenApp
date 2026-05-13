package org.emprenApp.detalle_pedido.infrastructure.controller;

import org.emprenApp.detalle_pedido.application.DetallePedidoAdapter;
import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoAddRequest;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.pedido.infrastructure.controller.PedidoController;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/detalle-pedido")
public class DetallePedidoController {

    private final static Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    private DetallePedidoAdapter detallePedidoAdapter;

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<DetallePedidoResponse>> obtenerDetallesPorPedido(@PathVariable Long pedidoId) throws BaseException {
        logger.info("REST Request - GET /pedido/{} para obtener detalles", pedidoId);
        return ResponseEntity.ok(detallePedidoAdapter.obtenerDetallesPorPedido(pedidoId));
    }

    @PostMapping("/add")
    public ResponseEntity<DetallePedidoResponse> agregarDetalle(@RequestBody DetallePedidoAddRequest request) throws BaseException {
        logger.info("REST Request - POST /add - Agregando detalle para pedido ID: {}", request.getPedidoId());
        DetallePedidoResponse response = detallePedidoAdapter.agregarDetalle(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> eliminarDetalle(@PathVariable Long id) throws BaseException {
        logger.info("REST Request - DELETE /remove/{} - Intento de eliminación", id);
        if (id == null || id <= 0) {
            logger.info("Se intentó eliminar un detalle con un ID inválido: {}", id);
            throw new ValidationException();
        }
        detallePedidoAdapter.eliminarDetalle(id);
        logger.info("REST Response - Detalle con ID {} eliminado exitosamente", id);
        return ResponseEntity.ok("Detalle eliminado exitosamente");
    }
}
