package org.emprenApp.detalle_pedido.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "DETALLE_PEDIDO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @Column(name = "PRECIO_UNITARIO", precision = 10, scale = 2, nullable = false)
    private Double precioUnitario;

    /* 
    @ManyToOne
    @JoinColumn(name = "PEDIDO_ID", nullable = false)
    private Pedido pedidoId; // pedido_id (FK a la tabla de pedidos)

    @ManyToOne
    @JoinColumn(name = "PRODUCTO_ID", nullable = false)
    private Producto productoId; // producto_id (FK a la tabla de productos)
    */
}
