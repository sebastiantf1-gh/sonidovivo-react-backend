package cl.duoc.sonidovivo_backend.auth.controller; 
import cl.duoc.sonidovivo_backend.auth.dto.UsuarioResponse;
import cl.duoc.sonidovivo_backend.auth.dto.UsuarioUpdateRequest;
import cl.duoc.sonidovivo_backend.auth.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioService.listarUsuarios();
    }

    @PutMapping("/{id}")
    public UsuarioResponse modificarUsuario(@PathVariable Long id, 
                                            @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.modificarUsuario(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
    }
}