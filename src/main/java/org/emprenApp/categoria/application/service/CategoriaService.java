package org.emprenApp.categoria.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.categoria.application.CategoriaAdapter;
import org.emprenApp.categoria.application.dto.CategoriaDTO;
import org.emprenApp.categoria.application.mapper.CategoriaMapper;
import org.emprenApp.categoria.domain.Categoria;
import org.emprenApp.categoria.domain.CategoriaRepository;
import org.emprenApp.categoria.infrastructure.request.CategoriaCreateRequest;
import org.emprenApp.categoria.infrastructure.request.CategoriaUpdateRequest;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService implements CategoriaAdapter {

    private final static Logger logger = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository categoriaRepository;
    private final CategoriaValidationService categoriaValidationService;

    @Override
    @Transactional
    public CategoriaDTO createCategoria(CategoriaCreateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            categoriaValidationService.validarCampos(request.getNombre(), request.getDescripcion(), request.getTipo());

            String nombre = normalizarNombre(request.getNombre());
            if (categoriaRepository.existsByNombreIgnoreCaseAndActiveTrue(nombre)) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }

            Categoria categoria = CategoriaMapper.INSTANCE.toEntity(request);
            Categoria created = categoriaRepository.save(categoria);
            logger.info("Categoria creada: {}", created.getNombre());
            return CategoriaMapper.INSTANCE.toDTO(created);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear categoria", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDTO getCategoriaById(Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return CategoriaMapper.INSTANCE.toDTO(categoriaRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> getCategoriasActivas() throws BaseException {
        try {
            return categoriaRepository.findAllByActiveTrue().stream()
                    .map(CategoriaMapper.INSTANCE::toDTO)
                    .toList();
        } catch (Exception e) {
            logger.error("Error al listar categorias activas", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public CategoriaDTO updateCategoria(CategoriaUpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ValidateGeneric.validateId(request.getId());

            Categoria categoria = categoriaRepository.findByIdAndActiveTrue(request.getId()).orElseThrow(NotFoundException::new);

            categoriaValidationService.validarCampos(request.getNombre(), request.getDescripcion(), request.getTipo());

            if (request.getNombre() != null) {
                String nombre = normalizarNombre(request.getNombre());
                if (categoriaRepository.existsByNombreIgnoreCaseAndActiveTrueAndIdNot(nombre, categoria.getId())) {
                    throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
                }
            }
            CategoriaMapper.INSTANCE.updateEntityFromRequest(request, categoria);

            Categoria updated = categoriaRepository.save(categoria);
            logger.info("Categoria actualizada: {}", updated.getId());
            return CategoriaMapper.INSTANCE.toDTO(updated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar categoria", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public boolean deleteCategoria(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            Categoria categoria = categoriaRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new);
            categoria.setActive(false);
            categoriaRepository.save(categoria);
            logger.info("Categoria eliminada (baja lógica) ID: {}", id);
            return true;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar la categoria ID: {}", id, e);
            throw new GenericException();
        }
    }

    // Misma normalizacion que aplica CategoriaMapper al guardar, para que el chequeo de duplicados compare lo que realmente queda en la base.
    private String normalizarNombre(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ");
    }
}
