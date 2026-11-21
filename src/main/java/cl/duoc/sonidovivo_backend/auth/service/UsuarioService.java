package cl.duoc.sonidovivo_backend.auth.service; 

import cl.duoc.sonidovivo_backend.auth.dto.UsuarioResponse;
import cl.duoc.sonidovivo_backend.auth.dto.UsuarioUpdateRequest;
import cl.duoc.sonidovivo_backend.model.Usuario;
import cl.duoc.sonidovivo_backend.repository.RolRepository;
import cl.duoc.sonidovivo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    // LISTAR TODOS
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::aRespuesta)
                .collect(Collectors.toList());
    }

    // MODIFICAR
    public UsuarioResponse modificarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setNombreUsuario(request.getNombre().trim());
        usuario.setApellidoUsuario(request.getApellido().trim());
        usuario.setNumeroTelefono(request.getNumeroTelefono());

        Usuario actualizado = usuarioRepository.save(usuario);
        return aRespuesta(actualizado);
    }

    // ELIMINAR (Físico)
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    // Método auxiliar para mapear la entidad a DTO de forma segura
    private UsuarioResponse aRespuesta(Usuario u) {
        String nombreRol = rolRepository.findById(u.getIdRol())
                .map(rol -> rol.getNombreRol())
                .orElse("Desconocido");

        return UsuarioResponse.builder()
                .id(u.getIdUsuario())
                .nombre(u.getNombreUsuario())
                .apellido(u.getApellidoUsuario())
                .email(u.getEmail())
                .numeroTelefono(u.getNumeroTelefono())
                .rol(nombreRol)
                .fechaRegistro(u.getFechaRegistro())
                .build();
    }
}