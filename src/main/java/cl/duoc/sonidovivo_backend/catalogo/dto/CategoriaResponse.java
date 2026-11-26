package cl.duoc.sonidovivo_backend.catalogo.dto;

import cl.duoc.sonidovivo_backend.model.Categoria;

public record CategoriaResponse(
        Long id,
        String nombre,
        String imagen,
        String textoAlternativo
) {
    public static CategoriaResponse desdeEntidad(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getImagen(),
                categoria.getTextoAlternativo()
        );
    }
}