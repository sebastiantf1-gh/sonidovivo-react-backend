package cl.duoc.sonidovivo_backend.repository;

import cl.duoc.sonidovivo_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    // Para validar antes de registrar
    boolean existsByEmail(String email);
}