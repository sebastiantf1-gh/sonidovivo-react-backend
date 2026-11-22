package cl.duoc.sonidovivo_backend.auth.controller;

import cl.duoc.sonidovivo_backend.auth.dto.UsuarioCreateRequest;
import cl.duoc.sonidovivo_backend.auth.dto.UsuarioResponse;
import cl.duoc.sonidovivo_backend.auth.dto.UsuarioUpdateRequest;
import cl.duoc.sonidovivo_backend.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "CRUD de usuarios (requiere JWT con rol Administrador)")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Lista todos los usuarios registrados (solo Administrador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o vencido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente")
    })
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un usuario por su ID (solo Administrador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o vencido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public UsuarioResponse obtenerUsuario(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario asignando su rol (solo Administrador)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o rol inexistente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o vencido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado")
    })
    public UsuarioResponse crearUsuario(@Valid @RequestBody UsuarioCreateRequest request) {
        return usuarioService.crearUsuario(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifica datos y/o rol de un usuario (solo Administrador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o rol inexistente"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o vencido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public UsuarioResponse modificarUsuario(@PathVariable Long id,
                                            @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.modificarUsuario(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario por su ID (solo Administrador)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o vencido"),
            @ApiResponse(responseCode = "403", description = "Rol insuficiente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "No puede eliminar su propia cuenta")
    })
    public void eliminarUsuario(@PathVariable Long id, Authentication authentication) {
        Long idActual = Long.valueOf(authentication.getName());
        usuarioService.eliminarUsuario(id, idActual);
    }
}