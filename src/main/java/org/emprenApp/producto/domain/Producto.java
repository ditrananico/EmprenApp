package org.emprenApp.producto.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.categoria.domain.Categoria;
import org.emprenApp.emprendimiento.domain.Emprendimiento;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TITULO", length = 100)
    private String titulo;

    @Column(name = "DESCRIPCION", length = 100)
    private String descripcion;

    @Column(name = "PRECIO",  precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "STOCK")
    private Integer stock;

    @Column(name = "STOCK_MINIMO")
    private Integer stockMinimo;

    @Column(name = "ACTIVE")
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "emprendimiento_id", nullable = false)
    private Emprendimiento emprendimiento;

    @ManyToOne
    @JoinColumn(name = "CATEGORIA_ID", nullable = false)
    private Categoria categoria;

}
