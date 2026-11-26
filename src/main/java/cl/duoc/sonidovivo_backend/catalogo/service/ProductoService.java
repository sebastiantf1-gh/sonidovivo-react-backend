package cl.duoc.sonidovivo_backend.catalogo.service;

import cl.duoc.sonidovivo_backend.catalogo.dto.ProductoRequest;
import cl.duoc.sonidovivo_backend.catalogo.dto.ProductoResponse;
import cl.duoc.sonidovivo_backend.catalogo.dto.StockUpdateRequest;
import cl.duoc.sonidovivo_backend.model.Categoria;
import cl.duoc.sonidovivo_backend.model.Producto;
import cl.duoc.sonidovivo_backend.repository.CategoriaRepository;
import cl.duoc.sonidovivo_backend.repository.DetallePedidoRepository;
import cl.duoc.sonidovivo_backend.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private static final String IMAGEN_POR_DEFECTO = "/img/placeholder.png";

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetallePedidoRepository detallePedidoRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Long categoriaId, String categoriaNombre, Boolean soloCriticos) {
        List<Producto> productos;
        if (Boolean.TRUE.equals(soloCriticos)) {
            productos = productoRepository.findProductosCriticos();
        } else if (categoriaId != null) {
            productos = productoRepository.findByCategoriaId(categoriaId);
        } else if (categoriaNombre != null && !categoriaNombre.isBlank()) {
            productos = productoRepository.findByCategoriaNombreIgnoreCase(categoriaNombre.trim());
        } else {
            productos = productoRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }

        return productos.stream()
                .map(ProductoResponse::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return ProductoResponse.desdeEntidad(buscarEntidad(id));
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorCodigo(String codigo) {
        Producto producto = productoRepository.findByCodigoIgnoreCase(codigo.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con código: " + codigo));
        return ProductoResponse.desdeEntidad(producto);
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String codigoLimpio = request.codigo().trim().toUpperCase();
        if (productoRepository.existsByCodigoIgnoreCase(codigoLimpio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un producto con el código: " + codigoLimpio);
        }

        Categoria categoria = buscarCategoria(request.categoriaId());

        Producto nuevo = Producto.builder()
                .codigo(codigoLimpio)
                .nombre(request.nombre().trim())
                .marca(request.marca().trim())
                .modelo(request.modelo().trim())
                .descripcionBreve(request.descripcionBreve() != null ? request.descripcionBreve().trim() : "")
                .precioClp(request.precioClp())
                .stock(request.stock())
                .stockMinimo(request.stockMinimo())
                .imagen(normalizarImagen(request.imagen()))
                .categoria(categoria)
                .build();

        return ProductoResponse.desdeEntidad(productoRepository.save(nuevo));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto existente = buscarEntidad(id);
        String codigoLimpio = request.codigo().trim().toUpperCase();

        productoRepository.findByCodigoIgnoreCase(codigoLimpio)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe otro producto con el código: " + codigoLimpio);
                });

        Categoria categoria = buscarCategoria(request.categoriaId());

        existente.setCodigo(codigoLimpio);
        existente.setNombre(request.nombre().trim());
        existente.setMarca(request.marca().trim());
        existente.setModelo(request.modelo().trim());
        existente.setDescripcionBreve(request.descripcionBreve() != null ? request.descripcionBreve().trim() : "");
        existente.setPrecioClp(request.precioClp());
        existente.setStock(request.stock());
        existente.setStockMinimo(request.stockMinimo());
        existente.setImagen(normalizarImagen(request.imagen()));
        existente.setCategoria(categoria);

        return ProductoResponse.desdeEntidad(productoRepository.save(existente));
    }

    @Transactional
    public ProductoResponse actualizarStock(Long id, StockUpdateRequest request) {
        Producto existente = buscarEntidad(id);
        existente.setStock(request.stock());
        return ProductoResponse.desdeEntidad(productoRepository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto existente = buscarEntidad(id);
        if (detallePedidoRepository.existsByProductoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el producto porque figura en pedidos registrados");
        }
        productoRepository.delete(existente);
    }

    private Producto buscarEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con id: " + id));
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada con id: " + categoriaId));
    }

    private String normalizarImagen(String imagen) {
        return (imagen == null || imagen.isBlank()) ? IMAGEN_POR_DEFECTO : imagen.trim();
    }
}