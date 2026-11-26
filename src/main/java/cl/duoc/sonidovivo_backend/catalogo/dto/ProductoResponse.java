package cl.duoc.sonidovivo_backend.catalogo.dto;

import cl.duoc.sonidovivo_backend.model.Producto;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String marca,
        String modelo,
        String descripcionBreve,
        Integer precioClp,
        Integer stock,
        Integer stockMinimo,
        String imagen,
        Long categoriaId,
        String categoriaNombre
) {
    public static ProductoResponse desdeEntidad(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getMarca(),
                producto.getModelo(),
                producto.getDescripcionBreve(),
                producto.getPrecioClp(),
                producto.getStock(),
                producto.getStockMinimo(),
                producto.getImagen(),
                producto.getCategoria().getId(),
                producto.getCategoria().getNombre()
        );
    }
}