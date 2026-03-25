package org.emprenApp.pedido.domain;

import org.emprenApp.shared.application.enums.EstadoPedidoEnum;
import org.emprenApp.shared.application.enums.EstadoUserEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Page<Pedido> findAllByEstado(EstadoUserEnum estado, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :userId AND (:status IS NULL OR p.status = :status)")
    Page<Pedido> findByUserIdAndOptionalStatus(@Param("userId") Long userId, @Param("status") EstadoPedidoEnum status, Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.emprendimiento.id = :emprendimientoId AND (:status IS NULL OR p.status = :status)")
    Page<Pedido> findByEmprendimientoIdAndStatusOptional(@Param("emprendimientoId") Long emprendimientoId, @Param("status") EstadoPedidoEnum status, Pageable pageable);
}
