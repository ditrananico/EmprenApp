package org.emprenApp.emprendimiento.infrastructure.controller;

import org.emprenApp.emprendimiento.EmprendimientoAdapter;
import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.infrastructure.mapper.EmprendimientoInfrastructureMapper;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.emprendimiento.infrastructure.response.EmprendimientoResponse;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/emprendimiento")
public class EmprendimientoController {

    private final EmprendimientoAdapter emprendimientoAdapter;

    public EmprendimientoController(EmprendimientoAdapter emprendimientoAdapter) {
        this.emprendimientoAdapter = emprendimientoAdapter;
    }

    @PostMapping("/create")
    public EmprendimientoResponse createEmprendimiento(
            @RequestBody @Validated EmprendimientoCreateRequest emprendimientoCreateRequest
    ) throws BaseException {
        EmprendimientoDTO emprendimientoDTO =
                emprendimientoAdapter.crearEmprendimiento(emprendimientoCreateRequest);
        return EmprendimientoInfrastructureMapper.INSTANCE.toResponse(emprendimientoDTO);
    }
}
