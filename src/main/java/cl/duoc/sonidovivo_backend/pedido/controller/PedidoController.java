package cl.duoc.sonidovivo_backend.pedido.controller;

import cl.duoc.sonidovivo_backend.pedido.dto.PedidoCreateRequest;
import cl.duoc.sonidovivo_backend.pedido.dto.PedidoEstadoRequest;
import cl.duoc.sonidovivo_backend.pedido.dto.PedidoResponse;
import cl.duoc.sonidovivo_backend.pedido.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/pedidos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pedidos", description = "Creación de compras, control de stock (409) y gestión de estados")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Crea un pedido validando stock disponible y descontándolo transaccionalmente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente para uno o más productos")
    })
    public ResponseEntity<PedidoResponse> crear(
            @Valid @RequestBody PedidoCreateRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crear(request, authentication));
    }

    @GetMapping
    @Operation(summary = "Lista pedidos (Cliente ve solo los suyos; Vendedor y Administrador ven todos)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    public List<PedidoResponse> listar(Authentication authentication) {
        return pedidoService.listar(authentication);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene el detalle de un pedido (Cliente solo puede ver los propios)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle del pedido"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Intento de acceso a un pedido ajeno"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public PedidoResponse obtenerPorId(@PathVariable Long id, Authentication authentication) {
        return pedidoService.obtenerPorId(id, authentication);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualiza el estado de un pedido (solo Administrador o Vendedor)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido ya se encuentra Entregado o Cancelado")
    })
    public PedidoResponse actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody PedidoEstadoRequest request
    ) {
        return pedidoService.actualizarEstado(id, request);
    }
}