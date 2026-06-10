package org.emprenApp.repartidor.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.emprendimiento.domain.Emprendimiento;
import org.emprenApp.shared.application.enums.ModalidadCobroRepartidorEnum;
import org.emprenApp.shared.application.enums.ModalidadLiquidacionRepartidorEnum;
import org.emprenApp.shared.application.enums.VehiculoRepartidorEnum;
import org.emprenApp.user.domain.User;

import java.math.BigDecimal;

@Entity
@Table(name = "repartidores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehiculo", nullable = false, length = 30)
    private VehiculoRepartidorEnum vehiculo;

    @ManyToOne
    @JoinColumn(name = "emprendimiento_id", nullable = true)
    private Emprendimiento emprendimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad_liquidacion", nullable = false, length = 30)
    private ModalidadLiquidacionRepartidorEnum modalidadLiquidacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad_de_cobro", nullable = false, length = 30)
    private ModalidadCobroRepartidorEnum modalidadDeCobro;

    @Column(name = "variable_de_cobro", nullable = false, precision = 12, scale = 2)
    private BigDecimal variableDeCobro;
}
