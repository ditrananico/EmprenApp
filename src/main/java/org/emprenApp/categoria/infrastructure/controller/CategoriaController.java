package org.emprenApp.categoria.infrastructure.controller;

import org.emprenApp.categoria.application.CategoriaAdapter;
import org.emprenApp.categoria.application.dto.CategoriaDTO;
import org.emprenApp.categoria.infrastructure.mapper.CategoriaInfrastructureMapper;
import org.emprenApp.categoria.infrastructure.request.CategoriaCreateRequest;
import org.emprenApp.categoria.infrastructure.request.CategoriaUpdateRequest;
import org.emprenApp.categoria.infrastructure.response.CategoriaResponse;
import org.emprenApp.shared.application.application.BaseRestController;
import org.emprenApp.shared.application.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/categoria")
public class CategoriaController extends BaseRestController {

    private final static Logger logger = LoggerFactory.getLogger(CategoriaController.class);

    @Autowired
    private CategoriaAdapter categoriaAdapter;

    @PostMapping("/")
    public ResponseEntity<CategoriaResponse> createCategoria(@RequestBody @Validated CategoriaCreateRequest request) throws BaseException {
        logger.info("Creando categoria: {}", request.getNombre());
        CategoriaDTO created = categoriaAdapter.createCategoria(request);
        return responseCreated(CategoriaInfrastructureMapper.INSTANCE.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> getCategoria(@PathVariable Long id) throws BaseException {
        CategoriaDTO dto = categoriaAdapter.getCategoriaById(id);
        return responseOk(CategoriaInfrastructureMapper.INSTANCE.toResponse(dto));
    }

    @GetMapping("/list")
    public ResponseEntity<List<CategoriaDTO>> getCategoriasActivas() throws BaseException {
        return responseOk(categoriaAdapter.getCategoriasActivas());
    }

    @PutMapping("/")
    public ResponseEntity<CategoriaResponse> updateCategoria(@RequestBody @Validated CategoriaUpdateRequest request) throws BaseException {
        logger.info("Actualizando categoria: {}", request.getId());
        CategoriaDTO updated = categoriaAdapter.updateCategoria(request);
        return responseOk(CategoriaInfrastructureMapper.INSTANCE.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCategoria(@PathVariable Long id) throws BaseException {
        logger.info("Eliminando categoria: {}", id);
        Boolean eliminada = categoriaAdapter.deleteCategoria(id);
        logger.info("Categoria eliminada: {}", id);
        return responseOk(eliminada);
    }
}
