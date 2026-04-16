package org.emprenApp.producto.application.service;

import org.emprenApp.producto.application.ProductoAdapter;
import org.emprenApp.producto.domain.ProductoRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private ProductoRepository productoRepository;
    private ProductoAdapter productoAdapter;
}
