package org.emprenApp.categoria.infrastructure.controller;

import org.emprenApp.categoria.application.CategoriaAdapter;
import org.emprenApp.detalle_pedido.infrastructure.controller.DetallePedidoController;
import org.emprenApp.shared.application.application.BaseRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/categoria")
public class CategoriaController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(DetallePedidoController.class);

    @Autowired
     private CategoriaAdapter categoriaAdapter;
}
