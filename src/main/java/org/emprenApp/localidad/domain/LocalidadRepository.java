package org.emprenApp.localidad.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {

    Optional<Localidad> findByIdAndActiveTrue(Long id);

    @Query("SELECT DISTINCT l.provincia FROM Localidad l WHERE l.active = true ORDER BY l.provincia")
    List<String> findProvincias();

    @Query("SELECT DISTINCT l.partido FROM Localidad l WHERE l.active = true " +
            "AND LOWER(l.provincia) = LOWER(:provincia) ORDER BY l.partido")
    List<String> findPartidos(@Param("provincia") String provincia);

    boolean existsByProvinciaIgnoreCaseAndPartidoIgnoreCaseAndLocalidadIgnoreCaseAndActiveTrue(
            String provincia, String partido, String localidad);

    boolean existsByProvinciaIgnoreCaseAndPartidoIgnoreCaseAndLocalidadIgnoreCaseAndActiveTrueAndIdNot(
            String provincia, String partido, String localidad, Long id);

    @Query("SELECT l FROM Localidad l WHERE l.active = true " +
            "AND (:provincia IS NULL OR LOWER(l.provincia) = LOWER(:provincia)) " +
            "AND (:partido IS NULL OR LOWER(l.partido) = LOWER(:partido)) " +
            "AND (:q IS NULL OR LOWER(l.localidad) LIKE LOWER(CONCAT('%', :q, '%')) ESCAPE '|') " +
            "ORDER BY l.provincia, l.partido, l.localidad")
    List<Localidad> buscar(@Param("provincia") String provincia,
                           @Param("partido") String partido,
                           @Param("q") String q,
                           Pageable pageable);
}
