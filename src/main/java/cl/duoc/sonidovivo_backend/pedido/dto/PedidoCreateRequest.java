package cl.duoc.sonidovivo_backend.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PedidoCreateRequest(
        @NotBlank(message = "El tipo de despacho es obligatorio ('retiro' o 'despacho')")
        String tipoDespacho,

        @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
        String direccionDespacho,

        @NotEmpty(message = "El pedido debe incluir al menos un producto")
        @Valid
        List<PedidoItemRequest> items
) {}