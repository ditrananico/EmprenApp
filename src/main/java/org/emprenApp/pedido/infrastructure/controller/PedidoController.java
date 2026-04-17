package org.emprenApp.pedido.infrastructure.controller;

import org.emprenApp.pedido.application.PedidoAdapter;
import org.emprenApp.pedido.application.dto.PedidoDTO;
import org.emprenApp.shared.application.enums.EstadoPedidoEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/pedido")
public class PedidoController {
    private final static Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Autowired
    private PedidoAdapter pedidoAdapter;

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> getPedidoByID(@PathVariable Long id) throws BaseException {
        return ResponseEntity.ok(pedidoAdapter.getPedidoByID(id));
    }

    @GetMapping("/usuario/{id}")
    public ResponseEntity<Page<PedidoDTO>> getAllPedidoByIDUserAndStatus(@PathVariable Long userId, @RequestParam(required = false) EstadoPedidoEnum status, Pageable pageable) throws BaseException {
        return ResponseEntity.ok(pedidoAdapter.getAllPedidoByUserIDAndStatus(userId, status, pageable));
    }

    @GetMapping("/emprendimiento/{emprendimientoId}")
    public ResponseEntity<Page<PedidoDTO>> getPedidosByEmprendimiento(@PathVariable Long emprendimientoId, @RequestParam(required = false) EstadoPedidoEnum status, Pageable pageable) throws BaseException {
        return ResponseEntity.ok(pedidoAdapter.getAllPedidoByEmprendimientoIDAndStatus(emprendimientoId, status, pageable));
    }
    /*
    get all pedidos paginable
    get pedido por id del pedido


    get pedido por id del usuario paginable
    get pedido por id del usuario + estado paginable

    get pedidos por id emprendimiento paginable
    get pedidos por estado y id emprendimiento paginable

     */
}
