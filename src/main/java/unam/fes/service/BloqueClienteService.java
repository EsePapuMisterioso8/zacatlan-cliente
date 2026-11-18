
package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BloqueClienteService {

    private final RestClient restClient;

    private static final ParameterizedTypeReference<List<Map<String, Object>>> TIPO_LISTA =
            new ParameterizedTypeReference<>() {};

    public BloqueClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Map<String, Object>> listarBloques() {
        List<Map<String, Object>> bloques = restClient.get()
                .uri("/api/bloques")
                .retrieve()
                .body(TIPO_LISTA);

        return bloques != null ? bloques : Collections.emptyList();
    }

    public void crearBloque(String nombre, String descripcion) {
        Map<String, Object> bloque = new HashMap<>();
        bloque.put("nombre", nombre);
        bloque.put("descripcion", descripcion);
        bloque.put("activo", true);

        restClient.post()
                .uri("/api/bloques")
                .body(bloque)
                .retrieve()
                .toBodilessEntity();
    }

    public void modificarBloque(
            Long id,
            String nombre,
            String descripcion) {

        Map<String, Object> bloque = new HashMap<>();
        bloque.put("nombre", nombre);
        bloque.put("descripcion", descripcion);

        restClient.put()
                .uri("/api/bloques/{id}", id)
                .body(bloque)
                .retrieve()
                .toBodilessEntity();
    }

    public void cambiarEstado(Long id, boolean activar) {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/bloques/{id}/estado")
                        .queryParam("activar", activar)
                        .build(id))
                .retrieve()
                .toBodilessEntity();
    }
}
