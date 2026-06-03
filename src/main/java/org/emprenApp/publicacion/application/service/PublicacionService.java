package org.emprenApp.publicacion.application.service;

import org.emprenApp.publicacion.application.PublicacionAdapter;
import org.emprenApp.publicacion.application.dto.PublicacionDTO;
import org.emprenApp.publicacion.application.mapper.PublicacionMapper;
import org.emprenApp.publicacion.domain.Publicacion;
import org.emprenApp.publicacion.domain.PublicacionRepository;
import org.emprenApp.publicacion.infrastructure.request.PublicacionCreateRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublicacionService implements PublicacionAdapter {

    private final static Logger logger = LoggerFactory.getLogger(PublicacionService.class);

    @Autowired private PublicacionRepository repository;
    @Autowired private PublicacionValidationService validationService;

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

}
