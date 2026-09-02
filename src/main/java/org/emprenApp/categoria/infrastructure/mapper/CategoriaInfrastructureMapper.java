package org.emprenApp.categoria.infrastructure.mapper;

import org.emprenApp.categoria.application.dto.CategoriaDTO;
import org.emprenApp.categoria.infrastructure.response.CategoriaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoriaInfrastructureMapper {
    CategoriaInfrastructureMapper INSTANCE = Mappers.getMapper(CategoriaInfrastructureMapper.class);

    CategoriaResponse toResponse(CategoriaDTO categoriaDTO);
}
