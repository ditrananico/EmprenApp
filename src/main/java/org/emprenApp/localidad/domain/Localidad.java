package org.emprenApp.localidad.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "localidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Localidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provincia", nullable = false, length = 100)
    private String provincia;

    @Column(name = "partido", nullable = false, length = 100)
    private String partido;

    @Column(name = "localidad", nullable = false, length = 100)
    private String localidad;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
