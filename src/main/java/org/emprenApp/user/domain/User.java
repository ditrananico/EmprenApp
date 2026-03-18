package org.emprenApp.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emprenApp.shared.application.enums.EstadoUserEnum;

import java.sql.Timestamp;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 60)
    private String password;

    @Column(name = "FECHA_CREACION", nullable = false)
    private Timestamp fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoUserEnum estado;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "APELLIDO", nullable = false, length = 100)
    private String apellido;

    @Column(name = "TELEFONO", length = 15)
    private String telefono;

}
