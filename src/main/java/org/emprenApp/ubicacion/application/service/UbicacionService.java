package org.emprenApp.ubicacion.application.service;

import lombok.RequiredArgsConstructor;
import org.emprenApp.localidad.domain.Localidad;
import org.emprenApp.localidad.domain.LocalidadRepository;
import org.emprenApp.shared.application.application.ValidateGeneric;
import org.emprenApp.shared.application.enums.ErrorCodeEnum;
import org.emprenApp.shared.application.enums.UserStatusEnum;
import org.emprenApp.shared.application.exception.BaseException;
import org.emprenApp.shared.application.exception.GenericException;
import org.emprenApp.shared.application.exception.NotFoundException;
import org.emprenApp.ubicacion.application.UbicacionAdapter;
import org.emprenApp.ubicacion.application.dto.UbicacionDTO;
import org.emprenApp.ubicacion.application.mapper.UbicacionMapper;
import org.emprenApp.ubicacion.domain.Ubicacion;
import org.emprenApp.ubicacion.domain.UbicacionRepository;
import org.emprenApp.ubicacion.infrastructure.request.UbicacionCreateRequest;
import org.emprenApp.ubicacion.infrastructure.request.UbicacionUpdateRequest;
import org.emprenApp.user.domain.User;
import org.emprenApp.user.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionService implements UbicacionAdapter {

    private final static Logger logger = LoggerFactory.getLogger(UbicacionService.class);

    private final UbicacionRepository ubicacionRepository;
    private final LocalidadRepository localidadRepository;
    private final UserRepository userRepository;
    private final UbicacionValidationService ubicacionValidationService;

    @Override
    @Transactional
    public UbicacionDTO createUbicacion(UbicacionCreateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ubicacionValidationService.validarCampos(request.getCalle(), request.getNumero(), request.getPiso(),
                    request.getDepto(), request.getReferencia(), request.getAlias());

            Localidad localidad = findLocalidad(request.getLocalidadId());
            User usuario = findUsuario(request.getUsuarioId());

            Ubicacion ubicacion = UbicacionMapper.INSTANCE.toEntity(request);
            ubicacion.setLocalidad(localidad);
            ubicacion.setUsuario(usuario);

            aplicarPredeterminada(ubicacion);

            Ubicacion created = ubicacionRepository.save(ubicacion);
            logger.info("Ubicacion creada ID: {} (usuarioId={})", created.getId(), request.getUsuarioId());
            return UbicacionMapper.INSTANCE.toDTO(created);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al crear ubicacion", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionDTO getUbicacionById(Long id) throws BaseException {
        ValidateGeneric.validateId(id);
        return UbicacionMapper.INSTANCE.toDTO(ubicacionRepository.findByIdAndActivoTrue(id).orElseThrow(NotFoundException::new));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionDTO> getUbicacionesByUsuario(Long userId) throws BaseException {
        try {
            ValidateGeneric.validateId(userId);
            return ubicacionRepository.findByUsuarioIdAndActivoTrue(userId).stream()
                    .map(UbicacionMapper.INSTANCE::toDTO)
                    .toList();
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al listar ubicaciones del usuario {}", userId, e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public UbicacionDTO updateUbicacion(UbicacionUpdateRequest request) throws BaseException {
        try {
            ValidateGeneric.validateNotNull(request);
            ValidateGeneric.validateId(request.getId());

            Ubicacion ubicacion = ubicacionRepository.findByIdAndActivoTrue(request.getId())
                    .orElseThrow(NotFoundException::new);

            // Sólo se editan entradas de libreta. Un snapshot de pedido / dirección de emprendimiento no se muta.
            if (ubicacion.getUsuario() == null) {
                throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
            }

            ubicacionValidationService.validarCampos(request.getCalle(), request.getNumero(), request.getPiso(),
                    request.getDepto(), request.getReferencia(), request.getAlias());

            if (request.getLocalidadId() != null
                    && (ubicacion.getLocalidad() == null
                        || !ubicacion.getLocalidad().getId().equals(request.getLocalidadId()))) {
                ubicacion.setLocalidad(findLocalidad(request.getLocalidadId()));
            }

            UbicacionMapper.INSTANCE.updateEntityFromRequest(request, ubicacion);

            if (request.getPredeterminada() != null) {
                aplicarPredeterminada(ubicacion);
            }

            Ubicacion updated = ubicacionRepository.save(ubicacion);
            logger.info("Ubicacion actualizada ID: {}", updated.getId());
            return UbicacionMapper.INSTANCE.toDTO(updated);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al actualizar ubicacion", e);
            throw new GenericException();
        }
    }

    @Override
    @Transactional
    public boolean deleteUbicacion(Long id) throws BaseException {
        try {
            ValidateGeneric.validateId(id);
            Ubicacion ubicacion = ubicacionRepository.findByIdAndActivoTrue(id).orElseThrow(NotFoundException::new);

            if (ubicacion.getUsuario() == null) {
                throw new BaseException(ErrorCodeEnum.INVALID_PARAMETERS);
            }

            ubicacion.setActivo(false);
            ubicacion.setPredeterminada(false);
            ubicacionRepository.save(ubicacion);
            logger.info("Ubicacion eliminada (baja lógica) ID: {}", id);
            return true;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error al eliminar la ubicacion ID: {}", id, e);
            throw new GenericException();
        }
    }

    private Localidad findLocalidad(Long localidadId) throws BaseException {
        ValidateGeneric.validateId(localidadId);
        return localidadRepository.findByIdAndActiveTrue(localidadId).orElseThrow(NotFoundException::new);
    }

    private User findUsuario(Long usuarioId) throws BaseException {
        if (usuarioId == null) {
            return null;
        }
        ValidateGeneric.validateId(usuarioId);
        return userRepository.findByIdAndStatus(usuarioId, UserStatusEnum.ACTIVE).orElseThrow(NotFoundException::new);
    }

    // Si esta ubicación queda marcada como predeterminada, se desmarca cualquier otra del mismo usuario.
    private void aplicarPredeterminada(Ubicacion ubicacion) {
        if (!Boolean.TRUE.equals(ubicacion.getPredeterminada()) || ubicacion.getUsuario() == null) {
            return;
        }
        List<Ubicacion> otras = ubicacionRepository
                .findByUsuarioIdAndPredeterminadaTrueAndActivoTrue(ubicacion.getUsuario().getId());
        for (Ubicacion otra : otras) {
            if (!otra.getId().equals(ubicacion.getId())) {
                otra.setPredeterminada(false);
            }
        }
        if (!otras.isEmpty()) {
            ubicacionRepository.saveAll(otras);
        }
    }
}
