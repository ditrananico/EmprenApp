package org.emprenApp.publicacion.application;

import org.emprenApp.publicacion.application.dto.PublicacionDTO;
import org.emprenApp.publicacion.infrastructure.request.PublicacionCreateRequest;
import org.emprenApp.publicacion.infrastructure.request.PublicacionUpdateRequest;
import org.emprenApp.shared.application.exception.BaseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublicacionAdapter {

    PublicacionDTO createPublicacion(PublicacionCreateRequest request) throws BaseException;
    PublicacionDTO getPublicacionById(Long id) throws BaseException;
    Page<PublicacionDTO> getPublicaciones(Pageable pageable) throws BaseException;
    PublicacionDTO updatePublicacion(Long id, PublicacionUpdateRequest request) throws BaseException;
    void deletePublicacion(Long id) throws BaseException;

}
