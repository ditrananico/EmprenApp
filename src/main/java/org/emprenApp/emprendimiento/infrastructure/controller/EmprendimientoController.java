package org.emprenApp.emprendimiento.infrastructure.controller;

import org.emprenApp.emprendimiento.application.EmprendimientoAdapter;
import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.application.service.EmprendimientoService;
import org.emprenApp.emprendimiento.infrastructure.mapper.EmprendimientoInfrastructureMapper;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.emprendimiento.infrastructure.response.EmprendimientoResponse;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/emprendimiento")
public class EmprendimientoController {

    private final static Logger logger = LoggerFactory.getLogger(EmprendimientoController.class);
    @Autowired private EmprendimientoAdapter emprendimientoAdapter;

    @PostMapping("/")
    public EmprendimientoResponse createEmprendimiento(
            @RequestBody @Validated EmprendimientoCreateRequest emprendimientoCreateRequest
    ) throws BaseException {
        logger.info("Se inicializa la creación del emprendimiento: {}", emprendimientoCreateRequest.toString());
        EmprendimientoDTO emprendimientoDTO =
                emprendimientoAdapter.createEmprendimiento(emprendimientoCreateRequest);
        return EmprendimientoInfrastructureMapper.INSTANCE.toResponse(emprendimientoDTO);
    }
}
