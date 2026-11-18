
package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import unam.fes.model.VentaResponseDTO;
import unam.fes.model.VentaDetalleClienteDTO;

import java.util.List;

@Service
public class HistorialVentasClienteService {

    private final RestClient restClient;

    public HistorialVentasClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    // Historial del vendedor autenticado
    public List<VentaResponseDTO> listarMisVentas() {

        return restClient.get()
                .uri("/api/ventas/vendedor")
                .retrieve()
                .body(new ParameterizedTypeReference<List<VentaResponseDTO>>() {});
    }

    // Historial de todas las ventas
    public List<VentaResponseDTO> listarTodasLasVentas() {

        return restClient.get()
                .uri("/api/ventas")
                .retrieve()
                .body(new ParameterizedTypeReference<List<VentaResponseDTO>>() {});
    }

    // Consultar ventas dentro de un rango de fechas
    public List<VentaResponseDTO> listarVentasPorRango(
            String fechaInicio,
            String fechaFin) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ventas/rango")
                        .queryParam("fechaInicio", fechaInicio)
                        .queryParam("fechaFin", fechaFin)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<VentaResponseDTO>>() {});
    }

    // Detalle de una venta
    public VentaDetalleClienteDTO obtenerDetalleVenta(Long idVenta) {

        return restClient.get()
                .uri("/api/ventas/{id}", idVenta)
                .retrieve()
                .body(VentaDetalleClienteDTO.class);
    }
}
