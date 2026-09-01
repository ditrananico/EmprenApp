package org.emprenApp.ubicacion.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ubicaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calle", nullable = false, length = 150)
    private String calle;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "piso", length = 10)
    private String piso;

    @Column(name = "depto", length = 10)
    private String depto;

    @Column(name = "referencia", length = 255)
    private String referencia;

    @ManyToOne
    @JoinColumn(name = "localidad_id", nullable = false)
    private Localidad localidad;
}
