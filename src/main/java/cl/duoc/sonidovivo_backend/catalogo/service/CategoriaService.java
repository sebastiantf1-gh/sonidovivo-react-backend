package cl.duoc.sonidovivo_backend.catalogo.service;

import cl.duoc.sonidovivo_backend.catalogo.dto.CategoriaRequest;
import cl.duoc.sonidovivo_backend.catalogo.dto.CategoriaResponse;
import cl.duoc.sonidovivo_backend.model.Categoria;
import cl.duoc.sonidovivo_backend.repository.CategoriaRepository;
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
public class CategoriaService {

    private static final String IMAGEN_POR_DEFECTO = "/img/placeholder.png";

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(CategoriaResponse::desdeEntidad)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = buscarEntidad(id);
        return CategoriaResponse.desdeEntidad(categoria);
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombreLimpio = request.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombreLimpio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una categoría con el nombre: " + nombreLimpio);
        }

        Categoria nueva = Categoria.builder()
                .nombre(nombreLimpio)
                .imagen(normalizarImagen(request.imagen()))
                .textoAlternativo(normalizarAlt(request.textoAlternativo(), nombreLimpio))
                .build();

        return CategoriaResponse.desdeEntidad(categoriaRepository.save(nueva));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria existente = buscarEntidad(id);
        String nombreLimpio = request.nombre().trim();

        categoriaRepository.findByNombreIgnoreCase(nombreLimpio)
                .filter(otra -> !otra.getId().equals(id))
                .ifPresent(otra -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe otra categoría con el nombre: " + nombreLimpio);
                });

        existente.setNombre(nombreLimpio);
        existente.setImagen(normalizarImagen(request.imagen()));
        existente.setTextoAlternativo(normalizarAlt(request.textoAlternativo(), nombreLimpio));

        return CategoriaResponse.desdeEntidad(categoriaRepository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria existente = buscarEntidad(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar la categoría porque tiene productos asociados");
        }
        categoriaRepository.delete(existente);
    }

    private Categoria buscarEntidad(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada con id: " + id));
    }

    private String normalizarImagen(String imagen) {
        return (imagen == null || imagen.isBlank()) ? IMAGEN_POR_DEFECTO : imagen.trim();
    }

    private String normalizarAlt(String alt, String nombre) {
        return (alt == null || alt.isBlank()) ? nombre : alt.trim();
    }
}