package org.emprenApp.localidad.application.mapper;

import org.emprenApp.localidad.application.dto.LocalidadDTO;
import org.emprenApp.localidad.domain.Localidad;
import org.emprenApp.localidad.infrastructure.request.LocalidadCreateRequest;
import org.emprenApp.localidad.infrastructure.request.LocalidadUpdateRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

//  un campo null del request NO pisa el valor actual de la entidad.
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocalidadMapper {

    LocalidadMapper INSTANCE = Mappers.getMapper(LocalidadMapper.class);

    LocalidadDTO toDTO(Localidad localidad);

    Localidad toEntity(LocalidadDTO localidadDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    Localidad toEntity(LocalidadCreateRequest request);

    // solo copia los campos no nulos del request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(LocalidadUpdateRequest request, @MappingTarget Localidad localidad);

    // Normaliza espacios de los campos de texto después de cualquier mapeo hacia Localidad.
    @AfterMapping
    default void limpiarBlancos(@MappingTarget Localidad localidad) {
        localidad.setProvincia(normalizar(localidad.getProvincia()));
        localidad.setPartido(normalizar(localidad.getPartido()));
        localidad.setLocalidad(normalizar(localidad.getLocalidad()));
        localidad.setCodigoPostal(normalizar(localidad.getCodigoPostal()));
    }

    // private -> MapStruct no la toma como método de mapeo implícito; sólo la usa @AfterMapping.
    private String normalizar(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
