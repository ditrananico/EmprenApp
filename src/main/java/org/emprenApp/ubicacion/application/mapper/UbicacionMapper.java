package org.emprenApp.ubicacion.application.mapper;

import org.emprenApp.localidad.application.mapper.LocalidadMapper;
import org.emprenApp.ubicacion.application.dto.UbicacionDTO;
import org.emprenApp.ubicacion.domain.Ubicacion;
import org.emprenApp.ubicacion.infrastructure.request.UbicacionCreateRequest;
import org.emprenApp.ubicacion.infrastructure.request.UbicacionUpdateRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

// uses = LocalidadMapper -> mapea el objeto anidado Localidad -> LocalidadDTO.
// un campo null del request NO pisa el valor actual.
@Mapper(uses = LocalidadMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UbicacionMapper {

    UbicacionMapper INSTANCE = Mappers.getMapper(UbicacionMapper.class);

    @Mapping(target = "usuarioId", source = "usuario.id")
    UbicacionDTO toDTO(Ubicacion ubicacion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "activo", constant = "true")
    Ubicacion toEntity(UbicacionCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "localidad", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "activo", ignore = true)
    void updateEntityFromRequest(UbicacionUpdateRequest request, @MappingTarget Ubicacion ubicacion);

    // Normaliza espacios de los campos de texto después de cualquier mapeo hacia Ubicacion.
    @AfterMapping
    default void limpiarBlancos(@MappingTarget Ubicacion ubicacion) {
        ubicacion.setCalle(normalizar(ubicacion.getCalle()));
        ubicacion.setPiso(normalizar(ubicacion.getPiso()));
        ubicacion.setDepto(normalizar(ubicacion.getDepto()));
        ubicacion.setReferencia(normalizar(ubicacion.getReferencia()));
        ubicacion.setAlias(normalizar(ubicacion.getAlias()));
    }

    private String normalizar(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
