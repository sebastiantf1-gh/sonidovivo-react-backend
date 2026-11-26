package cl.duoc.sonidovivo_backend.repository;

import cl.duoc.sonidovivo_backend.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    Optional<Producto> findByCodigoIgnoreCase(String codigo);
    boolean existsByCategoriaId(Long categoriaId);
    List<Producto> findByCategoriaId(Long categoriaId);
    List<Producto> findByCategoriaNombreIgnoreCase(String nombreCategoria);

    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo ORDER BY p.stock ASC")
    List<Producto> findProductosCriticos();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdConBloqueo(@Param("id") Long id);
}