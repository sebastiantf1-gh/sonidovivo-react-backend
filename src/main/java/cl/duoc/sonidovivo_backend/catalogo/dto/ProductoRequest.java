package cl.duoc.sonidovivo_backend.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres")
        String codigo,

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
        String nombre,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 100, message = "La marca no puede superar los 100 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 100, message = "El modelo no puede superar los 100 caracteres")
        String modelo,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcionBreve,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio no puede ser negativo")
        Integer precioClp,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo,

        @Size(max = 255, message = "La ruta de imagen no puede superar los 255 caracteres")
        String imagen,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId
) {}