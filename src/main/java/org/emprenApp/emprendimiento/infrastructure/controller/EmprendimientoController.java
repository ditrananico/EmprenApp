package org.emprenApp.emprendimiento.infrastructure.controller;

import org.emprenApp.emprendimiento.application.EmprendimientoAdapter;
import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.infrastructure.mapper.EmprendimientoInfrastructureMapper;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoUpdateRequest;
import org.emprenApp.emprendimiento.infrastructure.response.EmprendimientoResponse;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/emprendimiento")
public class EmprendimientoController {

    private final static Logger logger = LoggerFactory.getLogger(EmprendimientoController.class);
    @Autowired private EmprendimientoAdapter emprendimientoAdapter;

    @PostMapping
    public ResponseEntity<EmprendimientoResponse> createEmprendimiento(
            @RequestBody @Validated EmprendimientoCreateRequest emprendimientoCreateRequest
    ) throws BaseException {
        logger.info("Se inicializa la creación del emprendimiento: {}", emprendimientoCreateRequest.toString());
        EmprendimientoDTO emprendimientoDTO =
                emprendimientoAdapter.createEmprendimiento(emprendimientoCreateRequest);
        EmprendimientoResponse response = EmprendimientoInfrastructureMapper.INSTANCE.toResponse(emprendimientoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmprendimientoResponse> getEmprendimientoById(@PathVariable Long id) throws BaseException {
        EmprendimientoDTO emprendimientoDTO = emprendimientoAdapter.getEmprendimientoById(id);
        EmprendimientoResponse response = EmprendimientoInfrastructureMapper.INSTANCE.toResponse(emprendimientoDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<EmprendimientoResponse>> getEmprendimientos(Pageable pageable) throws BaseException {
        Page<EmprendimientoDTO> emprendimientos = emprendimientoAdapter.getEmprendimientos(pageable);
        Page<EmprendimientoResponse> response = EmprendimientoInfrastructureMapper.INSTANCE.toResponse(emprendimientos);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmprendimientoResponse> updateEmprendimiento(
            @PathVariable Long id,
            @RequestBody @Validated EmprendimientoUpdateRequest emprendimientoUpdateRequest
    ) throws BaseException {
        logger.info("Se inicializa la actualización del emprendimiento con id: {}", id);
        EmprendimientoDTO emprendimientoDTO =
                emprendimientoAdapter.updateEmprendimiento(id, emprendimientoUpdateRequest);
        EmprendimientoResponse response = EmprendimientoInfrastructureMapper.INSTANCE.toResponse(emprendimientoDTO);
        return ResponseEntity.ok(response);
    }

    // AGREGAR MÉTODO CAMBIAR ESTADO, enviar por query params GOOGLEAR (o chatgptear)
    // @PutMapping("/status/{id}")


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmprendimiento(@PathVariable Long id) throws BaseException {
        logger.info("Se inicializa la eliminación lógica del emprendimiento con id: {}", id);
        emprendimientoAdapter.deleteEmprendimiento(id);
        return ResponseEntity.noContent().build();
    }
}