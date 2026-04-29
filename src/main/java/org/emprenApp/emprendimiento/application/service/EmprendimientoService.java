package org.emprenApp.emprendimiento.application.service;

import org.emprenApp.emprendimiento.EmprendimientoAdapter;
import org.emprenApp.emprendimiento.application.dto.EmprendimientoDTO;
import org.emprenApp.emprendimiento.domain.Emprendimiento;
import org.emprenApp.emprendimiento.domain.EmprendimientoRepository;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.GenericException;
import org.springframework.stereotype.Service;

@Service
public class EmprendimientoService implements EmprendimientoAdapter {

    public EmprendimientoDTO crearEmprendimiento(EmprendimientoCreateRequest emprendimientoCreateRequest) throws GenericException {
        try {
            if (emprendimientoCreateRequest == null) throw new GenericException(ErrorCodeEnum.PARAMETROS_INCORRECTOS);

            Emprendimiento emprendimientoCreado = EmprendimientoRepository.sav
        }
    }
}
