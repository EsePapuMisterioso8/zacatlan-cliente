
package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import unam.fes.model.ProductoDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductoClienteService {

    private final RestClient restClient;

    public ProductoClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<ProductoDTO> obtenerTodos() {
        List<ProductoDTO> productos = restClient.get()
                .uri("/api/productos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoDTO>>() {});

        return productos != null ? productos : List.of();
    }

    public void registrarProducto(
            String nombre,
            String descripcion,
            Long idBloque) {

        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", nombre);
        producto.put("descripcion", descripcion);

        Map<String, Object> bloque = new HashMap<>();
        bloque.put("id", idBloque);

        producto.put("bloque", bloque);

        restClient.post()
                .uri("/api/productos")
                .body(producto)
                .retrieve()
                .toBodilessEntity();
    }

    public void cambiarEstado(Long id) {
        restClient.put()
                .uri("/api/productos/{id}/estado", id)
                .retrieve()
                .toBodilessEntity();
    }
}
