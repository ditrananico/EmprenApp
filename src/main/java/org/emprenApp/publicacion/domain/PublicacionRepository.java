package org.emprenApp.publicacion.domain;

import org.emprenApp.shared.application.enums.EstadoPublicacionEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    Optional<Publicacion> findByIdAndEstado(Long id, EstadoPublicacionEnum estado);

    Page<Publicacion> findAllByEstado(EstadoPublicacionEnum estado, Pageable pageable);
}
