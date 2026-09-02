package org.emprenApp.ubicacion.application.service;

import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.stereotype.Service;


@Service
public class UbicacionValidationService {

    private static final int CALLE_MAX = 150;
    private static final int PISO_MAX = 10;
    private static final int DEPTO_MAX = 10;
    private static final int REFERENCIA_MAX = 255;
    private static final int ALIAS_MAX = 50;

    public void validarCampos(String calle, Integer numero, String piso, String depto,
                              String referencia, String alias) throws BaseException {
        if (calle != null) {
            ValidateGeneric.validateNotBlank(calle);
            ValidateGeneric.validateMaxLength(calle.trim(), CALLE_MAX);
        }
        if (numero != null && numero <= 0) {
            throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
        }
        ValidateGeneric.validateMaxLength(piso, PISO_MAX);
        ValidateGeneric.validateMaxLength(depto, DEPTO_MAX);
        ValidateGeneric.validateMaxLength(referencia, REFERENCIA_MAX);
        ValidateGeneric.validateMaxLength(alias, ALIAS_MAX);
    }
}
