package org.emprenApp.emprendimiento.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.shared.application.enums.EstadoEmprendimientoEnum;
import org.emprenApp.ubicacion.domain.Ubicacion;

@Entity
@Table(name = "emprendimientos")
@Data
@AllArgsConstructor @NoArgsConstructor
public class Emprendimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 40)
    private String name;

    @Column(name = "DESCRIPTION", length = 250)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoEmprendimientoEnum estado;

    // Direccion del emp (si se muda se actualiza).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    // Faltaría usuario_id
}
