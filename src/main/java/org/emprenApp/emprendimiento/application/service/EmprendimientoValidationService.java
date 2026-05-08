package org.emprenApp.emprendimiento.application.service;

import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class EmprendimientoValidationService {
    public void validateCreateRequest(EmprendimientoCreateRequest request) throws ValidationException {
        if (request == null) throw new ValidationException();
        if (request.getName() == null || request.getName().isBlank()) throw new ValidationException();
        if (request.getDescription() == null || request.getDescription().isBlank()) throw new ValidationException();
        if (request.getName().length() > 40) throw new ValidationException(ErrorCodeEnum.INPUT_LENGTH);
        if (request.getDescription().length() > 250) throw new ValidationException(ErrorCodeEnum.INPUT_LENGTH);
    }
}
