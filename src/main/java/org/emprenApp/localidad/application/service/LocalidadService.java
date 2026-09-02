package org.emprenApp.localidad.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.localidad.application.LocalidadAdapter;
import org.emprenApp.localidad.application.dto.LocalidadDTO;
import org.emprenApp.localidad.application.mapper.LocalidadMapper;
import org.emprenApp.localidad.domain.Localidad;
import org.emprenApp.localidad.domain.LocalidadRepository;
import org.emprenApp.localidad.infrastructure.request.LocalidadCreateRequest;
import org.emprenApp.localidad.infrastructure.request.LocalidadUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalidadService implements LocalidadAdapter {

    private final static Logger logger = LoggerFactory.getLogger(LocalidadService.class);

    private static final int BUSQUEDA_LIMIT = 25;

    private final LocalidadRepository localidadRepository;
    private final LocalidadValidationService localidadValidationService;

    @Override
    @Transactional(readOnly = true)
    public LocalidadDTO getLocalidadById(Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return LocalidadMapper.INSTANCE.toDTO(localidadRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getProvincias() throws BaseException {
        try {
            return localidadRepository.findProvincias();
        } catch (Exception e) {
            logger.error("Error al listar provincias", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPartidos(String provincia) throws BaseException {
        try {
            ValidateGeneric.validateNotBlank(provincia);
            return localidadRepository.findPartidos(provincia.trim());
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al listar partidos de {}", provincia, e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalidadDTO> buscar(String provincia, String partido, String q) throws BaseException {
        try {
            String prov = blankToNull(provincia);
            String part = blankToNull(partido);
            String texto = q == null || q.isBlank() ? null : escaparLike(q.trim());

            Pageable page = (prov != null && part != null) ? Pageable.unpaged() : PageRequest.of(0, BUSQUEDA_LIMIT);

            //VER ESTE BUSCAR
            return localidadRepository.buscar(prov, part, texto, page).stream()
                    .map(LocalidadMapper.INSTANCE::toDTO)
                    .toList();
        } catch (Exception e) {
            logger.error("Error al buscar localidades (provincia={}, partido={}, q={})", provincia, partido, q, e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public LocalidadDTO createLocalidad(LocalidadCreateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            localidadValidationService.validarCampos(request.getProvincia(), request.getPartido(),
                    request.getLocalidad(), request.getCodigoPostal(), request.getLatitud(), request.getLongitud());

            if (localidadRepository.existsByProvinciaIgnoreCaseAndPartidoIgnoreCaseAndLocalidadIgnoreCaseAndActiveTrue(
                    request.getProvincia().trim(), request.getPartido().trim(), request.getLocalidad().trim())) {
                throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
            }

            Localidad localidad = LocalidadMapper.INSTANCE.toEntity(request);
            Localidad created = localidadRepository.save(localidad);
            logger.info("Localidad creada: {} / {} / {}", created.getProvincia(), created.getPartido(), created.getLocalidad());
            return LocalidadMapper.INSTANCE.toDTO(created);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear localidad", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public LocalidadDTO updateLocalidad(LocalidadUpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ValidateGeneric.validateId(request.getId());

            Localidad localidad = localidadRepository.findByIdAndActiveTrue(request.getId()).orElseThrow(NotFoundException::new);

            localidadValidationService.validarCampos(request.getProvincia(), request.getPartido(),
                    request.getLocalidad(), request.getCodigoPostal(), request.getLatitud(), request.getLongitud());

            if (request.getProvincia() != null || request.getPartido() != null || request.getLocalidad() != null) {
                String prov = request.getProvincia() != null ? request.getProvincia().trim() : localidad.getProvincia();
                String part = request.getPartido() != null ? request.getPartido().trim() : localidad.getPartido();
                String loc = request.getLocalidad() != null ? request.getLocalidad().trim() : localidad.getLocalidad();
                if (localidadRepository.existsByProvinciaIgnoreCaseAndPartidoIgnoreCaseAndLocalidadIgnoreCaseAndActiveTrueAndIdNot(
                        prov, part, loc, localidad.getId())) {
                    throw new BaseException(ErrorCodeEnum.DUPLICATE_KEY);
                }
            }

            LocalidadMapper.INSTANCE.updateEntityFromRequest(request, localidad);

            Localidad updated = localidadRepository.save(localidad);
            logger.info("Localidad actualizada: {}", updated.getId());
            return LocalidadMapper.INSTANCE.toDTO(updated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar localidad", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public boolean deleteLocalidad(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            Localidad localidad = localidadRepository.findByIdAndActiveTrue(id).orElseThrow(NotFoundException::new);
            localidad.setActive(false);
            localidadRepository.save(localidad);
            logger.info("Localidad eliminada (baja lógica) ID: {}", id);
            return true;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar la localidad ID: {}", id, e);
            throw new GenericException();
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String escaparLike(String valor) {
        return valor.replace("|", "||").replace("%", "|%").replace("_", "|_");
    }
}
