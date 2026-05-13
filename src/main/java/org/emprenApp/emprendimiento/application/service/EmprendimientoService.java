package org.emprenApp.emprendimiento.application.service;

import org.emprenApp.emprendimiento.application.EmprendimientoAdapter;
import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.application.mapper.EmprendimientoMapper;
import org.emprenApp.emprendimiento.domain.Emprendimiento;
import org.emprenApp.emprendimiento.domain.EmprendimientoRepository;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoUpdateRequest;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.EstadoEmprendimientoEnum;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.shared.application.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EmprendimientoService implements EmprendimientoAdapter {

    private final static Logger logger = LoggerFactory.getLogger(EmprendimientoService.class);

    @Autowired private EmprendimientoRepository repository;
    @Autowired private EmprendimientoValidationService validationService;

    @Override
    public EmprendimientoDTO createEmprendimiento(EmprendimientoCreateRequest emprendimientoCreateRequest) throws GenericException, ValidationException {
        try {
            validationService.validateCreateRequest(emprendimientoCreateRequest);
            Emprendimiento emprendimiento = repository.save(
                    EmprendimientoMapper.INSTANCE.toEntityFromRequest(emprendimientoCreateRequest)
            );
            return EmprendimientoMapper.INSTANCE.toDTO(emprendimiento);
        } catch (ValidationException validationException) {
            logger.error("ERROR No pasó el filtro de creación de emprendimiento. Nombre: {}",
                    emprendimientoCreateRequest != null ? emprendimientoCreateRequest.getName() : null);
            throw validationException;
        } catch (Exception exception) {
            logger.error("ERROR inesperado al crear emprendimiento {}", emprendimientoCreateRequest != null ? emprendimientoCreateRequest.getName() : null);
            throw new GenericException();
        }
    }

    @Override
    public EmprendimientoDTO getEmprendimientoById(Long id) throws GenericException, NotFoundException {
        if (id == null || id < 0) {
            throw new GenericException(ErrorCodeEnum.INVALID_PARAMETERS);
        }

        try {
            Emprendimiento emprendimiento = repository.findByIdAndEstado(id, EstadoEmprendimientoEnum.ACTIVO).orElseThrow(NotFoundException::new);
            return EmprendimientoMapper.INSTANCE.toDTO(emprendimiento);
        } catch (NotFoundException notFoundException) {
            logger.error("ERROR Emprendimiento con id: {} no encontrado", id);
            throw notFoundException;
        } catch (Exception exception) {
            logger.error("ERROR inesperado al buscar emprendimiento {}", id);
            throw new GenericException();
        }
    }

    public Page<EmprendimientoDTO> getEmprendimientos(Pageable pageable) throws GenericException {
        try {
            Page<Emprendimiento> emprendimientos = repository.findAllByEstado(EstadoEmprendimientoEnum.ACTIVO, pageable);
            return EmprendimientoMapper.INSTANCE.toPageDTO(emprendimientos);
        } catch (Exception exception) {
            logger.error("ERROR inesperado al buscar emprendimientos: {}", exception.getMessage());
            throw new GenericException();
        }
    }

    @Override
    public EmprendimientoDTO updateEmprendimiento(Long id, EmprendimientoUpdateRequest emprendimientoUpdateRequest) throws GenericException, NotFoundException, ValidationException {
        if (id == null || id < 0) {
            throw new GenericException(ErrorCodeEnum.INVALID_PARAMETERS);
        }

        try {
            validationService.validateUpdateRequest(emprendimientoUpdateRequest);

            Emprendimiento emprendimiento = repository.findByIdAndEstado(id, EstadoEmprendimientoEnum.ACTIVO)
                    .orElseThrow(NotFoundException::new);

            if (emprendimientoUpdateRequest.getName() != null) {
                emprendimiento.setName(emprendimientoUpdateRequest.getName());
            }
            if (emprendimientoUpdateRequest.getDescription() != null) {
                emprendimiento.setDescription(emprendimientoUpdateRequest.getDescription());
            }

            Emprendimiento updatedEmprendimiento = repository.save(emprendimiento);
            return EmprendimientoMapper.INSTANCE.toDTO(updatedEmprendimiento);
        } catch (ValidationException validationException) {
            logger.error("ERROR No pasó el filtro de actualización de emprendimiento. Id: {}", id);
            throw validationException;
        } catch (NotFoundException notFoundException) {
            logger.error("ERROR Emprendimiento con id: {} no encontrado para actualizar", id);
            throw notFoundException;
        } catch (Exception exception) {
            logger.error("ERROR inesperado al actualizar emprendimiento {}", id, exception);
            throw new GenericException();
        }
    }

    @Override
    public Boolean deleteEmprendimiento(Long id) throws GenericException, NotFoundException {
        if (id == null || id < 0) {
            throw new GenericException(ErrorCodeEnum.INVALID_PARAMETERS);
        }

        try {
            Emprendimiento emprendimiento = repository.findByIdAndEstado(id, EstadoEmprendimientoEnum.ACTIVO)
                    .orElseThrow(NotFoundException::new);

            emprendimiento.setEstado(EstadoEmprendimientoEnum.INACTIVO);
            repository.save(emprendimiento);
            return Boolean.TRUE;
        } catch (NotFoundException notFoundException) {
            logger.error("ERROR Emprendimiento con id: {} no encontrado para eliminar", id);
            throw notFoundException;
        } catch (Exception exception) {
            logger.error("ERROR inesperado al eliminar emprendimiento {}", id, exception);
            throw new GenericException();
        }
    }
}
