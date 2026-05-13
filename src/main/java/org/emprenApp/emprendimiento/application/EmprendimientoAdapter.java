package org.emprenApp.emprendimiento.application;

import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoUpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.shared.application.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmprendimientoAdapter {

    EmprendimientoDTO createEmprendimiento(EmprendimientoCreateRequest emprendimientoCreateRequest) throws BaseException;
    EmprendimientoDTO getEmprendimientoById(Long id) throws BaseException;
    Page<EmprendimientoDTO> getEmprendimientos(Pageable pageable) throws BaseException;
    EmprendimientoDTO updateEmprendimiento(Long id, EmprendimientoUpdateRequest emprendimientoUpdateRequest) throws BaseException;
    void deleteEmprendimiento(Long id) throws BaseException;

}
