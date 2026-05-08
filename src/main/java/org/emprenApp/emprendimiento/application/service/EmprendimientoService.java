package org.emprenApp.emprendimiento.application.service;

import org.emprenApp.emprendimiento.application.EmprendimientoAdapter;
import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.application.mapper.EmprendimientoMapper;
import org.emprenApp.emprendimiento.domain.Emprendimiento;
import org.emprenApp.emprendimiento.domain.EmprendimientoRepository;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmprendimientoService implements EmprendimientoAdapter {

    private final static Logger logger = LoggerFactory.getLogger(EmprendimientoService.class);

    private EmprendimientoRepository repository;
    private EmprendimientoValidationService validationService;

    public EmprendimientoDTO createEmprendimiento(EmprendimientoCreateRequest emprendimientoCreateRequest) throws GenericException, ValidationException {
        try {
            validationService.validateCreateRequest(emprendimientoCreateRequest);
            Emprendimiento emprendimientoCreado = repository.save(
                    EmprendimientoMapper.INSTANCE.toEntityFromRequest(emprendimientoCreateRequest)
            );
            return EmprendimientoMapper.INSTANCE.toDTO(emprendimientoCreado);
        } catch (ValidationException validationException) {
            logger.error("ERROR No pasó el filtro de creación de emprendimiento. Nombre:: {}",
                    emprendimientoCreateRequest != null ? emprendimientoCreateRequest.getName() : null);
            throw validationException;
        } catch (Exception exception) {
            logger.error("ERROR inesperado al crear emprendimiento:", exception);
            throw new GenericException();
        }
    }
}
