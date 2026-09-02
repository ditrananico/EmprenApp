package org.emprenApp.categoria.application.service;

import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.stereotype.Service;

@Service
public class CategoriaValidationService {

    private static final int NOMBRE_MAX = 100;
    private static final int DESCRIPCION_MAX = 255;
    private static final int TIPO_MAX = 50;

    public void validarCampos(String nombre, String descripcion, String tipo) throws BaseException {
        if (nombre != null) {
            ValidateGeneric.validateNotBlank(nombre);
            ValidateGeneric.validateMaxLength(nombre.trim(), NOMBRE_MAX);
        }
        ValidateGeneric.validateMaxLength(descripcion, DESCRIPCION_MAX);
        ValidateGeneric.validateMaxLength(tipo, TIPO_MAX);
    }
}
