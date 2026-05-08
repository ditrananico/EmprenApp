package org.emprenApp.emprendimiento.application;

import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.infrastructure.request.EmprendimientoCreateRequest;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.ValidationException;

public interface EmprendimientoAdapter {

    EmprendimientoDTO createEmprendimiento(EmprendimientoCreateRequest emprendimientoCreateRequest) throws GenericException, ValidationException;
//    EmprendimientoDTO getEmprendimientoById(Long id) throws GenericException, NotFoundException;
//    Page<EmprendimientoDTO> getEmprendimientos(Pageable pageable) throws GenericException;
//    EmprendimientoDTO updateEmprendimiento(Long id, EmprendimientoUpdateRequest emprendimientoUpdateRequest) throws GenericException, NotFoundException;
//    Boolean deleteEmprendimiento(Long id) throws GenericException, NotFoundException;

}
