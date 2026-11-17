package cl.duoc.sonidovivo_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(name = "nombre", unique = true, nullable = false, length = 100)
    private String nombreUsuario;

    @Column(name = "apellido", unique = true, nullable = false, length = 100)
    private String apellidoUsuario;

    @Column(nullable = false)
    private String password;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "numero_telefono", length = 20)
    private String numeroTelefono;

    @Column(name = "fecha_registro",nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "id_rol", nullable = false)
    private Long idRol;
}