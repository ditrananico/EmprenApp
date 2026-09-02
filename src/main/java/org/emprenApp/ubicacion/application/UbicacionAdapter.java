package org.emprenApp.ubicacion.application;

import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.ubicacion.application.dto.UbicacionDTO;
import org.emprenApp.ubicacion.infrastructure.request.UbicacionCreateRequest;
import org.emprenApp.ubicacion.infrastructure.request.UbicacionUpdateRequest;

import java.util.List;

public interface UbicacionAdapter {

    UbicacionDTO createUbicacion(UbicacionCreateRequest request) throws BaseException;

    UbicacionDTO getUbicacionById(Long id) throws BaseException;

    List<UbicacionDTO> getUbicacionesByUsuario(Long userId) throws BaseException;

    UbicacionDTO updateUbicacion(UbicacionUpdateRequest request) throws BaseException;

    boolean deleteUbicacion(Long id) throws BaseException;
}
