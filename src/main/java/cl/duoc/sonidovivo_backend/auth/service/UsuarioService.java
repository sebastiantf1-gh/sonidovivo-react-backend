package cl.duoc.sonidovivo_backend.auth.service;

import cl.duoc.sonidovivo_backend.auth.dto.UsuarioCreateRequest;
import cl.duoc.sonidovivo_backend.auth.dto.UsuarioResponse;
import cl.duoc.sonidovivo_backend.auth.dto.UsuarioUpdateRequest;
import cl.duoc.sonidovivo_backend.model.Rol;
import cl.duoc.sonidovivo_backend.model.Usuario;
import cl.duoc.sonidovivo_backend.repository.RolRepository;
import cl.duoc.sonidovivo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::aRespuesta)
                .collect(Collectors.toList());
    }

    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return aRespuesta(usuario);
    }

    public UsuarioResponse crearUsuario(UsuarioCreateRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        Rol rol = rolRepository.findByNombreRol(request.getRol().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rol inválido. Use Administrador, Vendedor o Cliente"));

        Usuario nuevo = Usuario.builder()
                .nombreUsuario(request.getNombre().trim())
                .apellidoUsuario(request.getApellido().trim())
                .email(email)
                .numeroTelefono(request.getNumeroTelefono())
                .password(passwordEncoder.encode(request.getPassword()))
                .idRol(rol.getId())
                .build();

        return aRespuesta(usuarioRepository.save(nuevo));
    }

    public UsuarioResponse modificarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setNombreUsuario(request.getNombre().trim());
        usuario.setApellidoUsuario(request.getApellido().trim());
        usuario.setNumeroTelefono(request.getNumeroTelefono());

        if (request.getRol() != null && !request.getRol().isBlank()) {
            Rol rol = rolRepository.findByNombreRol(request.getRol().trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Rol inválido. Use Administrador, Vendedor o Cliente"));
            usuario.setIdRol(rol.getId());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return aRespuesta(actualizado);
    }

    public void eliminarUsuario(Long id, Long idAdministradorActual) {
        if (id.equals(idAdministradorActual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No puedes eliminar tu propia cuenta de administrador");
        }
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponse aRespuesta(Usuario u) {
        String nombreRol = rolRepository.findById(u.getIdRol())
                .map(Rol::getNombreRol)
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