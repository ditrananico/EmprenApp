package org.emprenApp.producto.infrastructure.controller;


import org.emprenApp.producto.application.ProductoAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/producto")
public class ProductoController {

    @Autowired ProductoAdapter productoAdapter;


}
