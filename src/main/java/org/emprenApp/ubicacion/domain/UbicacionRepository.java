package org.emprenApp.ubicacion.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    Optional<Ubicacion> findByIdAndActivoTrue(Long id);

    List<Ubicacion> findByUsuarioIdAndActivoTrue(Long usuarioId);

    List<Ubicacion> findByUsuarioIdAndPredeterminadaTrueAndActivoTrue(Long usuarioId);
}
