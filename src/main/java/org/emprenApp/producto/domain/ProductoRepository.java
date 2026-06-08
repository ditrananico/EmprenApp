package org.emprenApp.producto.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Page<Producto> findByCategoriaIdAndActiveTrue(Long categoriaId, Pageable pageable);

    Page<Producto> findByEmprendimientoIdAndActiveTrue(Long emprendimientoId, Pageable pageable);

    @Query("SELECT p FROM Producto p " +
            "WHERE p.active = true " +
            "AND (LOWER(p.titulo) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Producto> searchProductosActivos(@Param("query") String query, Pageable pageable);
}
