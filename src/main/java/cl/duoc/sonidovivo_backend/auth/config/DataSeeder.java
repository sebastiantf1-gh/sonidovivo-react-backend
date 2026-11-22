package cl.duoc.sonidovivo_backend.auth.config;

import cl.duoc.sonidovivo_backend.model.Rol;
import cl.duoc.sonidovivo_backend.model.Usuario;
import cl.duoc.sonidovivo_backend.repository.RolRepository;
import cl.duoc.sonidovivo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Cuentas ficticias de prueba para la demostración (se almacenan con hash BCrypt).
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearSiNoExiste("Admin", "SonidoVivo", "admin@duoc.cl", "Admin1234", "+56911111111", "Administrador");
        crearSiNoExiste("Vendedor", "SonidoVivo", "vendedor@duoc.cl", "Vendedor1234", "+56922222222", "Vendedor");
        crearSiNoExiste("Cliente", "SonidoVivo", "cliente@gmail.com", "Cliente1234", "+56933333333", "Cliente");
    }

    private void crearSiNoExiste(String nombre, String apellido, String email,
                                 String passwordPlano, String telefono, String nombreRol) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        Rol rol = rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + nombreRol + " en la BD"));

        Usuario usuario = Usuario.builder()
                .nombreUsuario(nombre)
                .apellidoUsuario(apellido)
                .email(email)
                .numeroTelefono(telefono)
                .password(passwordEncoder.encode(passwordPlano))
                .idRol(rol.getId())
                .build();

        usuarioRepository.save(usuario);
    }
}