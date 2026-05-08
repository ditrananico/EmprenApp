package org.emprenApp.emprendimiento.application.service;

import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.GenericException;
import org.springframework.stereotype.Service;

@Service
public class EmprendimientoValidationService {
    public void validateCreateRequest(EmprendimientoCreateRequest request) throws GenericException {
        if (request == null) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
        if (request.getName() == null || request.getName().isBlank()) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
        if (request.getDescription() == null || request.getDescription().isBlank()) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
        if (request.getName().length() > 40) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
        if (request.getDescription().length() > 250) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);
    }
}
