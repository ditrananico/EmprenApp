package org.emprenApp.detalle_pedido.infrastructure.controller;

import org.emprenApp.detalle_pedido.application.DetallePedidoAdapter;
import org.emprenApp.detalle_pedido.infrastructure.request.DetallePedidoAddRequest;
import org.emprenApp.detalle_pedido.infrastructure.response.DetallePedidoResponse;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("v1/detalle-pedido")
public class DetallePedidoController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(DetallePedidoController.class);

    @Autowired
    private DetallePedidoAdapter detallePedidoAdapter;
    private ValidateGeneric validate;


    @GetMapping("/{pedidoId}")
    public ResponseEntity<DetallePedidoResponse> obtenerDetallePedidoId(@PathVariable Long pedidoId) throws BaseException {
        logger.info("REST Request - GET /{} para obtener detallePedido", pedidoId);
        validate.validateId(pedidoId);
        return responseOk(detallePedidoAdapter.getDetallePedidoByPedidoId(pedidoId));
    }

    @PostMapping("/")
    public ResponseEntity<DetallePedidoResponse> agregarDetallePedido(@RequestBody DetallePedidoAddRequest request) throws BaseException {
        logger.info("REST Request - POST /add - Agregando detalle para pedido ID: {}", request.getPedidoId());
        validate.validateNotNull(request);
        if (request.getItemsDetallePedido() == null || request.getItemsDetallePedido().isEmpty()) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
        return responseCreated(detallePedidoAdapter.agregarDetallePedido(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarDetallePedido(@PathVariable Long id) throws BaseException{
        logger.info("REST Request - DELETE /{} - Eliminando un DetallePedido", id);
        validate.validateId(id);
        detallePedidoAdapter.eliminarDetalle(id);
        return responseMessage("DetallePedido eliminado exitosamente");
    }
}
