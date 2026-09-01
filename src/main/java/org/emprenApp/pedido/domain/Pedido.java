package org.emprenApp.pedido.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.emprendimiento.domain.Emprendimiento;
import org.emprenApp.repartidor.domain.Repartidor;
import org.emprenApp.shared.application.enums.EstadoPedidoEnum;
import org.emprenApp.shared.application.enums.MetodoPagoEnum;
import org.emprenApp.ubicacion.domain.Ubicacion;
import org.emprenApp.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario; // usuario_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id", nullable = true)
    private Repartidor repartidor;

    @ManyToOne
    @JoinColumn(name = "emprendimiento_id", nullable = false)
    private Emprendimiento emprendimiento;

   @Column(name = "FECHA_CREACION", nullable = false)
   private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_FIN_PROCESO" )
    private LocalDateTime fechaFinProceso;

    @Column(name = "FECHA_FINALIZACION")
    private LocalDateTime fechaFinalizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_destino_id", nullable = false)
    private Ubicacion direccionDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPagoEnum metodoPago;

    @Column(name = "TOTAL", precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "COSTO_ENVIO", precision = 10, scale = 2)
    private BigDecimal costoEnvio;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoPedidoEnum status;
}
