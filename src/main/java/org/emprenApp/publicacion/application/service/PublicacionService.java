package org.emprenApp.publicacion.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.emprendimiento.application.mapper.EmprendimientoMapper;
import org.emprenApp.publicacion.application.PublicacionAdapter;
import org.emprenApp.publicacion.application.dto.PublicacionDTO;
import org.emprenApp.publicacion.application.mapper.PublicacionMapper;
import org.emprenApp.publicacion.domain.Publicacion;
import org.emprenApp.publicacion.domain.PublicacionRepository;
import org.emprenApp.publicacion.infrastructure.request.PublicacionCreateRequest;
import org.emprenApp.publicacion.infrastructure.request.PublicacionUpdateRequest;
import org.emprenApp.shared.application.enums.EstadoPublicacionEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.shared.application.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicacionService implements PublicacionAdapter {

    private final static Logger logger = LoggerFactory.getLogger(PublicacionService.class);

    private final PublicacionRepository repository;
    private final PublicacionValidationService validationService;

    @Override
    public PublicacionDTO createPublicacion(PublicacionCreateRequest request) throws BaseException {
        try {
            validationService.validateCreateRequest(request);
            Publicacion publicacion = repository.save(PublicacionMapper.INSTANCE.toEntityFromRequest(request));
            return PublicacionMapper.INSTANCE.toDto(publicacion);
        } catch (ValidationException validationException) {
            logger.error("No pasó el filtro de creación la publicación con título: {}", request != null ? request.getTitulo() : null);
            throw validationException;
        } catch (Exception exception) {
            logger.error("Ocurrió un error inesperado al crear publicación con título: {}", request != null ? request.getTitulo() : null);
            throw new GenericException();
        }
    }

    @Override
    public PublicacionDTO getPublicacionById(Long id) throws BaseException {
        if (id == null || id < 0) {
            logger.error("No se pudo buscar Publicacion por id inválido");
            throw new ValidationException();
        }
        try {
            Publicacion publicacion = repository.findByIdAndEstado(id, EstadoPublicacionEnum.ACTIVO).orElseThrow(NotFoundException::new);
            return PublicacionMapper.INSTANCE.toDto(publicacion);
        } catch (NotFoundException notFoundException) {
            logger.error("Publicacion con id {} no encontrado", id);
            throw notFoundException;
        } catch (Exception exception) {
            logger.error("Error inesperado al buscar publicacion {}", id);
            throw exception;
        }
    }

    @Override
    public Page<PublicacionDTO> getPublicaciones(Pageable pageable) throws BaseException {
        try {
            Page<Publicacion> publicaciones = repository.findAllByEstado(EstadoPublicacionEnum.ACTIVO, pageable);
            return PublicacionMapper.INSTANCE.toPageDTO(publicaciones);
        } catch (Exception exception) {
            logger.error("Error inesperado al buscar publicaciones: {}", exception.getMessage());
            throw new GenericException();
        }
    }

    @Override
    public PublicacionDTO updatePublicacion(Long id, PublicacionUpdateRequest request) throws BaseException {
        if (id == null || id < 0) {
            logger.error("No se pudo actualizar publicación por id inválido");
            throw new ValidationException();
        }
        try {
            validationService.validateUpdateRequest(request);

            Publicacion publicacion = repository.findByIdAndEstado(id, EstadoPublicacionEnum.ACTIVO).orElseThrow(NotFoundException::new);

            if (request.getTitulo() != null) {
                publicacion.setTitulo(request.getTitulo());
            }
            if (request.getDescripcion() != null) {
                publicacion.setDescripcion(request.getDescripcion());
            }

            Publicacion updatedPublicacion = repository.save(publicacion);
            return PublicacionMapper.INSTANCE.toDto(updatedPublicacion);

        } catch (NotFoundException notFoundException) {
            logger.error("Publicación con id {} no encontrada para actualizar", id);
            throw notFoundException;
        } catch (Exception exception) {
            logger.error("Error inesperado al eliminar publicación con id {}", id);
            throw new GenericException();
        }
    }

    @Override
    public void deletePublicacion(Long id) throws BaseException {

    }

}
