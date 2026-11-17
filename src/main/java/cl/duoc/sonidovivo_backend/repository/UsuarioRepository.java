package cl.duoc.sonidovivo_backend.repository;

import cl.duoc.sonidovivo_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    // Para validar antes de registrar
    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByEmail(String email);
}