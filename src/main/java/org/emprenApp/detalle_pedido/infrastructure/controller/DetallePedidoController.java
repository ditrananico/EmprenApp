package org.emprenApp.detalle_pedido.infrastructure.controller;

import org.emprenApp.detalle_pedido.application.DetallePedidoAdapter;
import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoAddRequest;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/detalle-pedido")
public class DetallePedidoController {

    @Autowired
    private DetallePedidoAdapter detallePedidoAdapter;

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<DetallePedidoResponse>> obtenerDetallesPorPedido(@PathVariable Long pedidoId) throws GenericException {
        return ResponseEntity.ok(detallePedidoAdapter.obtenerDetallesPorPedido(pedidoId));
    }

    @PostMapping("/add")
    public ResponseEntity<DetallePedidoResponse> agregarDetalle(@RequestBody DetallePedidoAddRequest request) throws GenericException, NotFoundException {
        return ResponseEntity.ok(detallePedidoAdapter.agregarDetalle(request));
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> eliminarDetalle(@PathVariable Long id) throws GenericException, NotFoundException {
        detallePedidoAdapter.eliminarDetalle(id);
        return ResponseEntity.ok("Detalle eliminado exitosamente");
    }
}
