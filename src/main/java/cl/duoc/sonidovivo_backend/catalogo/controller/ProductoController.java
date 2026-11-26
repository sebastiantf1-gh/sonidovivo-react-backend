package cl.duoc.sonidovivo_backend.catalogo.controller;

import cl.duoc.sonidovivo_backend.catalogo.dto.ProductoRequest;
import cl.duoc.sonidovivo_backend.catalogo.dto.ProductoResponse;
import cl.duoc.sonidovivo_backend.catalogo.dto.StockUpdateRequest;
import cl.duoc.sonidovivo_backend.catalogo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "CRUD del catálogo de instrumentos y actualización de stock")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Lista productos con filtros opcionales por categoría o stock crítico (público)")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    public List<ProductoResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean criticos
    ) {
        return productoService.listar(categoriaId, categoria, criticos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por ID (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ProductoResponse obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Obtiene un producto por su código SKU (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ProductoResponse obtenerPorCodigo(@PathVariable String codigo) {
        return productoService.obtenerPorCodigo(codigo);
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crea un nuevo producto (Administrador o Vendedor)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "409", description = "Código de producto duplicado")
    })
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualiza un producto existente (Administrador o Vendedor)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Producto o categoría no encontrados"),
            @ApiResponse(responseCode = "409", description = "Código de producto duplicado")
    })
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/stock")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualiza directamente el stock de un producto (Administrador o Vendedor)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock actualizado"),
            @ApiResponse(responseCode = "400", description = "Stock inválido"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ProductoResponse actualizarStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return productoService.actualizarStock(id, request);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Elimina un producto sin pedidos asociados (Administrador o Vendedor)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Producto asociado a pedidos existentes")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}