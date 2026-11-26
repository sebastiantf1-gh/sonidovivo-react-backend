package cl.duoc.sonidovivo_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "imagen", nullable = false, length = 255)
    private String imagen;

    @Column(name = "texto_alternativo", nullable = false, length = 150)
    private String textoAlternativo;
}