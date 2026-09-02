package org.emprenApp.localidad.infrastructure.controller;

import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.localidad.application.LocalidadAdapter;
import org.emprenApp.localidad.application.dto.LocalidadDTO;
import org.emprenApp.localidad.infrastructure.mapper.LocalidadInfrastructureMapper;
import org.emprenApp.localidad.infrastructure.request.LocalidadCreateRequest;
import org.emprenApp.localidad.infrastructure.request.LocalidadUpdateRequest;
import org.emprenApp.localidad.infrastructure.response.LocalidadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/localidad")
public class LocalidadController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(LocalidadController.class);

    @Autowired
    private LocalidadAdapter localidadAdapter;

    @GetMapping("/{id}")
    public ResponseEntity<LocalidadResponse> getLocalidad(@PathVariable Long id) throws BaseException {
        LocalidadDTO dto = localidadAdapter.getLocalidadById(id);
        return responseOk(LocalidadInfrastructureMapper.INSTANCE.toResponse(dto));
    }

    @GetMapping("/provincias")
    public ResponseEntity<List<String>> getProvincias() throws BaseException {
        return responseOk(localidadAdapter.getProvincias());
    }

    @GetMapping("/partidos")
    public ResponseEntity<List<String>> getPartidos(@RequestParam String provincia) throws BaseException {
        return responseOk(localidadAdapter.getPartidos(provincia));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocalidadDTO>> buscar(
            @RequestParam(required = false) String provincia,
            @RequestParam(required = false) String partido,
            @RequestParam(required = false) String q) throws BaseException {
        return responseOk(localidadAdapter.buscar(provincia, partido, q));
    }

    // Rol Admin
    @PostMapping("/")
    public ResponseEntity<LocalidadResponse> createLocalidad(@RequestBody @Validated LocalidadCreateRequest request) throws BaseException {
        logger.info("Creando localidad: {} / {} / {}", request.getProvincia(), request.getPartido(), request.getLocalidad());
        LocalidadDTO created = localidadAdapter.createLocalidad(request);
        return responseCreated(LocalidadInfrastructureMapper.INSTANCE.toResponse(created));
    }

    // Rol Admin
    @PutMapping("/")
    public ResponseEntity<LocalidadResponse> updateLocalidad(@RequestBody @Validated LocalidadUpdateRequest request) throws BaseException {
        logger.info("Actualizando localidad: {}", request.getId());
        LocalidadDTO updated = localidadAdapter.updateLocalidad(request);
        return responseOk(LocalidadInfrastructureMapper.INSTANCE.toResponse(updated));
    }

    // Rol Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteLocalidad(@PathVariable Long id) throws BaseException {
        logger.info("Eliminando localidad: {}", id);
        Boolean eliminada = localidadAdapter.deleteLocalidad(id);
        logger.info("Localidad eliminada: {}", id);
        return responseOk(eliminada);
    }
}
