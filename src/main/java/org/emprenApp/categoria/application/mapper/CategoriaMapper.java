package org.emprenApp.categoria.application.mapper;

import org.emprenApp.categoria.application.dto.CategoriaDTO;
import org.emprenApp.categoria.domain.Categoria;
import org.emprenApp.categoria.infrastructure.request.CategoriaCreateRequest;
import org.emprenApp.categoria.infrastructure.request.CategoriaUpdateRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

//  un campo null del request NO pisa el valor actual de la entidad.
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoriaMapper {

    CategoriaMapper INSTANCE = Mappers.getMapper(CategoriaMapper.class);

    CategoriaDTO toDTO(Categoria categoria);

    Categoria toEntity(CategoriaDTO categoriaDTO);

    @Mapping(target = "active", constant = "true")
    Categoria toEntity(CategoriaCreateRequest request);

    // solo copia los campos no nulos del request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(CategoriaUpdateRequest request, @MappingTarget Categoria categoria);

    // Normaliza los espacios del nombre despues de cualquier mapeo hacia Categoria.
    @AfterMapping
    default void limpiarBlancosEnElNombre(@MappingTarget Categoria categoria) {
        if (categoria.getNombre() != null) {
            categoria.setNombre(categoria.getNombre().trim().replaceAll("\\s+", " "));
        }
    }
}
