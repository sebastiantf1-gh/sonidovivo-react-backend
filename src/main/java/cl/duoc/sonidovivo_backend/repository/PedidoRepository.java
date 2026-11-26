package cl.duoc.sonidovivo_backend.repository;

import cl.duoc.sonidovivo_backend.model.Pedido;
import cl.duoc.sonidovivo_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioOrderByFechaCreacionDesc(Usuario usuario);
    List<Pedido> findAllByOrderByFechaCreacionDesc();
}