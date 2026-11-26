package cl.duoc.sonidovivo_backend.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @Size(max = 255, message = "La ruta de imagen no puede superar los 255 caracteres")
        String imagen,

        @Size(max = 150, message = "El texto alternativo no puede superar los 150 caracteres")
        String textoAlternativo
) {}