package cl.duoc.sonidovivo_backend.auth.service;

import cl.duoc.sonidovivo_backend.auth.dto.*;
import cl.duoc.sonidovivo_backend.auth.security.JwtService;
import cl.duoc.sonidovivo_backend.model.Rol;
import cl.duoc.sonidovivo_backend.model.Usuario;
import cl.duoc.sonidovivo_backend.repository.RolRepository;
import cl.duoc.sonidovivo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String ROL_INICIAL = "Cliente";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // Mismo error si el correo no existe o si la contraseña está mala (no se delata cuál falló).
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(this::credencialesInvalidas);
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw credencialesInvalidas();
        }

        Rol rol = buscarRol(usuario.getIdRol());
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(usuario, rol.getNombreRol()))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .usuario(aRespuesta(usuario, rol.getNombreRol()))
                .build();
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }

        // El rol lo asigna el servidor: nadie puede registrarse como Administrador.
        Rol rol = rolRepository.findByNombreRol(ROL_INICIAL)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + ROL_INICIAL + " en la base de datos"));

        Usuario nuevo = Usuario.builder()
                .nombreUsuario(request.getNombre().trim())
                .apellidoUsuario(request.getApellido().trim())
                .email(email)
                .numeroTelefono(request.getNumeroTelefono())
                .password(passwordEncoder.encode(request.getPassword()))
                .idRol(rol.getId())
                .build();
        return aRespuesta(usuarioRepository.save(nuevo), rol.getNombreRol());
    }

    // El id sale del token ya validado, nunca de algo que mande el cliente.
    public UsuarioResponse perfil(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
        return aRespuesta(usuario, buscarRol(usuario.getIdRol()).getNombreRol());
    }

    private Rol buscarRol(Long idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalStateException("Rol inexistente: " + idRol));
    }

    private ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }

    private UsuarioResponse aRespuesta(Usuario u, String nombreRol) {
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
