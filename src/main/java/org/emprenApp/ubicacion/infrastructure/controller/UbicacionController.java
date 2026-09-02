package org.emprenApp.ubicacion.infrastructure.controller;

import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.ubicacion.application.UbicacionAdapter;
import org.emprenApp.ubicacion.application.dto.UbicacionDTO;
import org.emprenApp.ubicacion.infrastructure.mapper.UbicacionInfrastructureMapper;
import org.emprenApp.ubicacion.infrastructure.request.UbicacionCreateRequest;
import org.emprenApp.ubicacion.infrastructure.request.UbicacionUpdateRequest;
import org.emprenApp.ubicacion.infrastructure.response.UbicacionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/ubicacion")
public class UbicacionController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(UbicacionController.class);

    @Autowired
    private UbicacionAdapter ubicacionAdapter;

    @PostMapping("/")
    public ResponseEntity<UbicacionResponse> createUbicacion(@RequestBody @Validated UbicacionCreateRequest request) throws BaseException {
        logger.info("Creando ubicacion (localidadId={}, usuarioId={})", request.getLocalidadId(), request.getUsuarioId());
        UbicacionDTO created = ubicacionAdapter.createUbicacion(request);
        return responseCreated(UbicacionInfrastructureMapper.INSTANCE.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UbicacionResponse> getUbicacion(@PathVariable Long id) throws BaseException {
        UbicacionDTO dto = ubicacionAdapter.getUbicacionById(id);
        return responseOk(UbicacionInfrastructureMapper.INSTANCE.toResponse(dto));
    }

    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<UbicacionDTO>> getUbicacionesByUsuario(@PathVariable Long userId) throws BaseException {
        return responseOk(ubicacionAdapter.getUbicacionesByUsuario(userId));
    }

    @PutMapping("/")
    public ResponseEntity<UbicacionResponse> updateUbicacion(@RequestBody @Validated UbicacionUpdateRequest request) throws BaseException {
        logger.info("Actualizando ubicacion: {}", request.getId());
        UbicacionDTO updated = ubicacionAdapter.updateUbicacion(request);
        return responseOk(UbicacionInfrastructureMapper.INSTANCE.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteUbicacion(@PathVariable Long id) throws BaseException {
        logger.info("Eliminando ubicacion: {}", id);
        Boolean eliminada = ubicacionAdapter.deleteUbicacion(id);
        logger.info("Ubicacion eliminada: {}", id);
        return responseOk(eliminada);
    }
}
