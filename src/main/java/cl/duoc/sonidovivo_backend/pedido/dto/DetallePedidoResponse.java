package cl.duoc.sonidovivo_backend.pedido.dto;

import cl.duoc.sonidovivo_backend.model.DetallePedido;

public record DetallePedidoResponse(
        Long productoId,
        String productoCodigo,
        String productoNombre,
        String productoImagen,
        Integer cantidad,
        Integer precioUnitario,
        Integer subtotal
) {
    public static DetallePedidoResponse desdeEntidad(DetallePedido detalle) {
        return new DetallePedidoResponse(
                detalle.getProducto().getId(),
                detalle.getProducto().getCodigo(),
                detalle.getProducto().getNombre(),
                detalle.getProducto().getImagen(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }
}