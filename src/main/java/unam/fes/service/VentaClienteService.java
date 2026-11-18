
package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VentaClienteService {

    private final RestClient restClient;

    public VentaClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public void registrarVenta(List<Map<String, Object>> items) {

        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("items", items);

        restClient.post()
                .uri("/api/ventas")
                .body(solicitud)
                .retrieve()
                .toBodilessEntity();
    }
}
