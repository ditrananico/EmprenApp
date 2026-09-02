package org.emprenApp.categoria.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByIdAndActiveTrue(Long id);

    List<Categoria> findAllByActiveTrue();

    boolean existsByNombreIgnoreCaseAndActiveTrue(String nombre);

    boolean existsByNombreIgnoreCaseAndActiveTrueAndIdNot(String nombre, Long id);
}
