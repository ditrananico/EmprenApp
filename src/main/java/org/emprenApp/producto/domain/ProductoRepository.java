package org.emprenApp.producto.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByIdAndActiveTrue(Long id);

    Page<Producto> findByCategoriaIdAndActiveTrue(Long categoriaId, Pageable pageable);

    Page<Producto> findByEmprendimientoIdAndActiveTrue(Long emprendimientoId, Pageable pageable);

    @Query("SELECT p FROM Producto p " +
            "WHERE p.active = true " +
            "AND (LOWER(p.titulo) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '|' " +
            "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '|')")
    Page<Producto> searchProductosActivos(@Param("query") String query, Pageable pageable);
}
