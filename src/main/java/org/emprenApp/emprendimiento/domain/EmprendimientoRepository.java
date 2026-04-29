package org.emprenApp.emprendimiento.domain;

import org.emprenApp.shared.application.enums.EstadoEmprendimientoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmprendimientoRepository extends JpaRepository<Emprendimiento, Long> {

    Optional<Emprendimiento> findByIdAndEstado(Long id, EstadoEmprendimientoEnum estado);

    Page<Emprendimiento> findAllByEstado(EstadoEmprendimientoEnum estado, Pageable pageable);
}
