package cl.duoc.sonidovivo_backend.repository;

import cl.duoc.sonidovivo_backend.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository <Rol, Long>{
    Optional<Rol> findByNombreRol(String nombreRol);
}