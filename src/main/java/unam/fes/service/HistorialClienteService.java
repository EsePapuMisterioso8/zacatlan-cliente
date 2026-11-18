
package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import unam.fes.model.HistorialDTO;

import java.util.List;

@Service
public class HistorialClienteService {

    private final RestClient restClient;

    public HistorialClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<HistorialDTO> listarHistorial() {

        List<HistorialDTO> historial = restClient.get()
                .uri("/api/historial")
                .retrieve()
                .body(new ParameterizedTypeReference<List<HistorialDTO>>() {});

        return historial != null ? historial : List.of();
    }
}
