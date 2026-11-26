package cl.duoc.sonidovivo_backend.pedido.dto;

import jakarta.validation.constraints.NotBlank;

public record PedidoEstadoRequest(
        @NotBlank(message = "El estado es obligatorio")
        String estado
) {}