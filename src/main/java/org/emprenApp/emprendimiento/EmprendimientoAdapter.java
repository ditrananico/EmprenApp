package org.emprenApp.emprendimiento;

import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmprendimientoAdapter {

    EmprendimientoDTO crearEmprendimiento(EmprendimientoCreateRequest emprendimientoCreateRequest) throws GenericException;
//    EmprendimientoDTO getEmprendimientoById(Long id) throws GenericException, NotFoundException;
//    Page<EmprendimientoDTO> getEmprendimientos(Pageable pageable) throws GenericException;
//    EmprendimientoDTO updateEmprendimiento(Long id, EmprendimientoUpdateRequest emprendimientoUpdateRequest) throws GenericException, NotFoundException;
//    Boolean deleteEmprendimiento(Long id) throws GenericException, NotFoundException;

}
