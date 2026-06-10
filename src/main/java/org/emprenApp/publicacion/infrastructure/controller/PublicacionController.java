package org.emprenApp.publicacion.infrastructure.controller;

import org.emprenApp.publicacion.application.PublicacionAdapter;
import org.emprenApp.publicacion.application.dto.PublicacionDTO;
import org.emprenApp.publicacion.application.mapper.PublicacionMapper;
import org.emprenApp.publicacion.infrastructure.mapper.PublicacionInfrastructureMapper;
import org.emprenApp.publicacion.infrastructure.request.PublicacionCreateRequest;
import org.emprenApp.publicacion.infrastructure.response.PublicacionResponse;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/publicacion")
public class PublicacionController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(PublicacionController.class);
    @Autowired private PublicacionAdapter publicacionAdapter;

    @PostMapping
    public ResponseEntity<PublicacionResponse> createPublicacion(
            @RequestBody @Validated PublicacionCreateRequest request
            ) throws BaseException {
        logger.info("Creating publicacion with request: {}", request);
        PublicacionDTO dto = publicacionAdapter.createPublicacion(request);
        PublicacionResponse response = PublicacionInfrastructureMapper.INSTANCE.toResponse(dto);
        return responseCreated(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponse> getPublicacionById(Long id) throws BaseException {
        PublicacionDTO dto = publicacionAdapter.getPublicacionById(id);
        PublicacionResponse response = PublicacionInfrastructureMapper.INSTANCE.toResponse(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PublicacionResponse>> getPublicaciones(Pageable pageable) throws BaseException {
        Page<PublicacionDTO> publicaciones = publicacionAdapter.getPublicaciones(pageable);
        Page<PublicacionResponse> response = PublicacionMapper.INSTANCE.toResponse(publicaciones);
        return ResponseEntity.ok(response);
    }

}

