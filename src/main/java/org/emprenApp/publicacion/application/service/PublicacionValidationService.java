package org.emprenApp.publicacion.application.service;

import org.emprenApp.publicacion.infrastructure.request.PublicacionCreateRequest;
import org.emprenApp.publicacion.infrastructure.request.PublicacionUpdateRequest;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class PublicacionValidationService {

    public void validateCreateRequest(PublicacionCreateRequest request) throws ValidationException {
        if (request == null) throw new ValidationException();
        if (request.getTitulo() == null || request.getTitulo().isBlank()) throw new ValidationException();
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()) throw new ValidationException();
        if (request.getTitulo().length() > 100) throw new ValidationException(ErrorCodeEnum.INPUT_LENGTH);
        if (request.getDescripcion().length() > 250) throw new ValidationException(ErrorCodeEnum.INPUT_LENGTH);
    }

    public void validateUpdateRequest(PublicacionUpdateRequest request) throws ValidationException {
        if (request == null) throw new ValidationException();

        boolean hasTitulo = request.getTitulo() != null;
        boolean hasDescripcion = request.getDescripcion() != null;
        if (!hasTitulo && !hasDescripcion) throw new ValidationException();

        if (hasTitulo) {
            if (request.getTitulo().isBlank()) throw new ValidationException();
            if (request.getTitulo().length() > 100) throw new ValidationException(ErrorCodeEnum.INPUT_LENGTH);
        }

        if (hasDescripcion) {
            if (request.getDescripcion().isBlank()) throw new ValidationException();
            if (request.getDescripcion().length() > 250) throw new ValidationException(ErrorCodeEnum.INPUT_LENGTH);
        }
    }
}
