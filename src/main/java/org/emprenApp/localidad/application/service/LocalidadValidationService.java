package org.emprenApp.localidad.application.service;

import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class LocalidadValidationService {

    private static final int TEXTO_MAX = 100;
    private static final int CP_MAX = 20;

    private static final BigDecimal LAT_MIN = new BigDecimal("-90");
    private static final BigDecimal LAT_MAX = new BigDecimal("90");
    private static final BigDecimal LON_MIN = new BigDecimal("-180");
    private static final BigDecimal LON_MAX = new BigDecimal("180");

    public void validarCampos(String provincia, String partido, String localidad, String codigoPostal,
                              BigDecimal latitud, BigDecimal longitud) throws BaseException {

        validarTexto(provincia);
        validarTexto(partido);
        validarTexto(localidad);

        ValidateGeneric.validateMaxLength(codigoPostal, CP_MAX);

        validarRango(latitud, LAT_MIN, LAT_MAX);
        validarRango(longitud, LON_MIN, LON_MAX);
    }

    private void validarTexto(String valor) throws BaseException {
        if (valor != null) {
            ValidateGeneric.validateNotBlank(valor);
            ValidateGeneric.validateMaxLength(valor.trim(), TEXTO_MAX);
        }
    }

    private void validarRango(BigDecimal valor, BigDecimal min, BigDecimal max) throws BaseException {
        if (valor != null && (valor.compareTo(min) < 0 || valor.compareTo(max) > 0)) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
    }
}
