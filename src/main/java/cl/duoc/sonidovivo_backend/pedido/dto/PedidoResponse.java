package cl.duoc.sonidovivo_backend.pedido.dto;

import cl.duoc.sonidovivo_backend.model.Pedido;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        String codigoPedido,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        LocalDateTime fechaCreacion,
        String estado,
        String tipoDespacho,
        String direccionDespacho,
        Integer total,
        List<DetallePedidoResponse> items
) {
    public static PedidoResponse desdeEntidad(Pedido pedido) {
        String nombreCompleto = (pedido.getUsuario().getNombreUsuario() + " "
                + pedido.getUsuario().getApellidoUsuario()).trim();
        List<DetallePedidoResponse> detalles = pedido.getDetalles()
                .stream()
                .map(DetallePedidoResponse::desdeEntidad)
                .toList();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getCodigoPedido(),
                pedido.getUsuario().getIdUsuario(),
                nombreCompleto,
                pedido.getUsuario().getEmail(),
                pedido.getFechaCreacion(),
                pedido.getEstado(),
                pedido.getTipoDespacho(),
                pedido.getDireccionDespacho(),
                pedido.getTotal(),
                detalles
        );
    }
}