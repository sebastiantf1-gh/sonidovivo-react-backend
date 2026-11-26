package cl.duoc.sonidovivo_backend.pedido.service;

import cl.duoc.sonidovivo_backend.model.DetallePedido;
import cl.duoc.sonidovivo_backend.model.Pedido;
import cl.duoc.sonidovivo_backend.model.Producto;
import cl.duoc.sonidovivo_backend.model.Usuario;
import cl.duoc.sonidovivo_backend.pedido.dto.PedidoCreateRequest;
import cl.duoc.sonidovivo_backend.pedido.dto.PedidoEstadoRequest;
import cl.duoc.sonidovivo_backend.pedido.dto.PedidoItemRequest;
import cl.duoc.sonidovivo_backend.pedido.dto.PedidoResponse;
import cl.duoc.sonidovivo_backend.repository.PedidoRepository;
import cl.duoc.sonidovivo_backend.repository.ProductoRepository;
import cl.duoc.sonidovivo_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of(
            "En preparación",
            "Despachado",
            "Entregado",
            "Cancelado"
    );

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PedidoResponse crear(PedidoCreateRequest request, Authentication authentication) {
        Usuario cliente = obtenerUsuarioAutenticado(authentication);

        String tipoDespacho = request.tipoDespacho().trim().toLowerCase();
        if (!tipoDespacho.equals("retiro") && !tipoDespacho.equals("despacho")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de despacho inválido. Valores permitidos: 'retiro' o 'despacho'");
        }

        String direccion = request.direccionDespacho() != null ? request.direccionDespacho().trim() : "";
        if (tipoDespacho.equals("despacho") && direccion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La dirección de despacho es obligatoria cuando el tipo es 'despacho'");
        }

        // Consolida cantidades por productoId y ordena por ID para evitar bloqueos cruzados.
        Map<Long, Integer> cantidadesPorProducto = new TreeMap<>();
        for (PedidoItemRequest item : request.items()) {
            cantidadesPorProducto.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        Pedido pedido = Pedido.builder()
                .codigoPedido(generarCodigoPedido())
                .usuario(cliente)
                .estado("En preparación")
                .tipoDespacho(tipoDespacho)
                .direccionDespacho(tipoDespacho.equals("retiro") ? "Retiro en tienda Sonido Vivo" : direccion)
                .total(0)
                .build();

        int totalCalculado = 0;

        for (Map.Entry<Long, Integer> entrada : cantidadesPorProducto.entrySet()) {
            Long productoId = entrada.getKey();
            Integer cantidadSolicitada = entrada.getValue();

            Producto producto = productoRepository.findByIdConBloqueo(productoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto no encontrado con id: " + productoId));

            if (producto.getStock() < cantidadSolicitada) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para " + producto.getNombre() + " (" + producto.getCodigo()
                                + "). Disponible: " + producto.getStock() + ", solicitado: " + cantidadSolicitada);
            }

            producto.setStock(producto.getStock() - cantidadSolicitada);
            productoRepository.save(producto);

            int precioUnitario = producto.getPrecioClp();
            int subtotal = precioUnitario * cantidadSolicitada;
            totalCalculado += subtotal;

            DetallePedido detalle = DetallePedido.builder()
                    .producto(producto)
                    .cantidad(cantidadSolicitada)
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .build();

            pedido.agregarDetalle(detalle);
        }

        pedido.setTotal(totalCalculado);
        Pedido guardado = pedidoRepository.save(pedido);
        return PedidoResponse.desdeEntidad(guardado);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(Authentication authentication) {
        List<Pedido> pedidos;
        if (esPersonal(authentication)) {
            pedidos = pedidoRepository.findAllByOrderByFechaCreacionDesc();
        } else {
            Usuario cliente = obtenerUsuarioAutenticado(authentication);
            pedidos = pedidoRepository.findByUsuarioOrderByFechaCreacionDesc(cliente);
        }

        return pedidos.stream()
                .map(PedidoResponse::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPorId(Long id, Authentication authentication) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pedido no encontrado con id: " + id));

        if (!esPersonal(authentication)) {
            Long idAutenticado = extraerIdUsuario(authentication);
            if (!pedido.getUsuario().getIdUsuario().equals(idAutenticado)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "No tienes permiso para acceder a este pedido");
            }
        }

        return PedidoResponse.desdeEntidad(pedido);
    }

    @Transactional
    public PedidoResponse actualizarEstado(Long id, PedidoEstadoRequest request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pedido no encontrado con id: " + id));

        String nuevoEstado = request.estado().trim();
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado inválido. Valores permitidos: En preparación, Despachado, Entregado, Cancelado");
        }

        String estadoActual = pedido.getEstado();
        if (estadoActual.equals("Cancelado") || estadoActual.equals("Entregado")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede cambiar el estado de un pedido que ya se encuentra " + estadoActual);
        }

        // Si el pedido se cancela, se restituye el stock de todos sus productos.
        if (nuevoEstado.equals("Cancelado")) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = productoRepository.findByIdConBloqueo(detalle.getProducto().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Producto asociado no encontrado"));
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        pedido.setEstado(nuevoEstado);
        return PedidoResponse.desdeEntidad(pedidoRepository.save(pedido));
    }

    private Usuario obtenerUsuarioAutenticado(Authentication authentication) {
        Long idUsuario = extraerIdUsuario(authentication);
        return usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"));
    }

    private Long extraerIdUsuario(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identificador de sesión inválido");
        }
    }

    private boolean esPersonal(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_VENDEDOR"));
    }

    private String generarCodigoPedido() {
        String sufijo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + sufijo;
    }
}