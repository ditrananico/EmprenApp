package org.emprenApp.localidad.application;

import org.emprenApp.localidad.application.dto.LocalidadDTO;
import org.emprenApp.localidad.infrastructure.request.LocalidadCreateRequest;
import org.emprenApp.localidad.infrastructure.request.LocalidadUpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;

import java.util.List;

public interface LocalidadAdapter {

    LocalidadDTO getLocalidadById(Long id) throws BaseException;

    List<String> getProvincias() throws BaseException;

    List<String> getPartidos(String provincia) throws BaseException;

    List<LocalidadDTO> buscar(String provincia, String partido, String q) throws BaseException;

    LocalidadDTO createLocalidad(LocalidadCreateRequest request) throws BaseException;

    LocalidadDTO updateLocalidad(LocalidadUpdateRequest request) throws BaseException;

    boolean deleteLocalidad(Long id) throws BaseException;
}
