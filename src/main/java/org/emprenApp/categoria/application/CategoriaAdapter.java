package org.emprenApp.categoria.application;

import org.emprenApp.categoria.application.dto.CategoriaDTO;
import org.emprenApp.categoria.infrastructure.request.CategoriaCreateRequest;
import org.emprenApp.categoria.infrastructure.request.CategoriaUpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;

import java.util.List;

public interface CategoriaAdapter {

    CategoriaDTO createCategoria(CategoriaCreateRequest request) throws BaseException;

    CategoriaDTO getCategoriaById(Long id) throws BaseException;

    List<CategoriaDTO> getCategoriasActivas() throws BaseException;

    CategoriaDTO updateCategoria(CategoriaUpdateRequest request) throws BaseException;

    boolean deleteCategoria(Long id) throws BaseException;
}
