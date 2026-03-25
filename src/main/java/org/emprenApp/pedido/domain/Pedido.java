package org.emprenApp.pedido.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.shared.application.enums.EstadoPedidoEnum;

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

   /* @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // usuario_id

    @ManyToOne
    @JoinColumn(name = "delivery_person_id")
    private User deliveryPerson;

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Emprendimiento emprendimiento;
    */
   @Column(name = "FECHA_CREACION", nullable = false)
   private LocalDateTime fechaCreacion;

    @Column(name = "FECHA_FIN_PROCESO" )
    private LocalDateTime fechaFinProceso;

    @Column(name = "FECHA_FINALIZACION")
    private LocalDateTime fechaFinalizacion;

   /* @ManyToOne
    @JoinColumn(name = "direccion_destino")
    private Address direccionDestino; // direccion_destino (FK a una tabla de direcciones)
*/
   @Column(name = "METODO_PAGO", length = 30)
   private String metodoPago;

    @Column(name = "TOTAL", precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "COSTO_ENVIO", precision = 10, scale = 2)
    private BigDecimal costoEnvio;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoPedidoEnum status;
}
