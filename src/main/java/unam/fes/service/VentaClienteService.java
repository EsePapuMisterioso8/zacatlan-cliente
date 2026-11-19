
package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VentaClienteService {

    private final RestClient restClient;

    private static final ParameterizedTypeReference<List<Map<String, Object>>> TIPO_LISTA =
            new ParameterizedTypeReference<>() {};

    public VentaClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    // Registrar una venta
    public void registrarVenta(List<Map<String, Object>> items) {

        Map<String, Object> solicitud = new HashMap<>();
        solicitud.put("items", items);

        restClient.post()
                .uri("/api/ventas")
                .body(solicitud)
                .retrieve()
                .toBodilessEntity();
    }

    // Consultar todas las ventas
    public List<Map<String, Object>> listarTodasLasVentas() {

        return restClient.get()
                .uri("/api/ventas")
                .retrieve()
                .body(TIPO_LISTA);
    }

    // Consultar ventas de un día
    public List<Map<String, Object>> listarVentasPorDia(LocalDate fecha) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ventas/dia")
                        .queryParam("fecha", fecha)
                        .build())
                .retrieve()
                .body(TIPO_LISTA);
    }

    // Consultar ventas de un vendedor en un día específico
    public List<Map<String, Object>> listarVentasPorVendedorYDia(
            Long idVendedor,
            LocalDate fecha) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ventas/vendedor/{idVendedor}/dia")
                        .queryParam("fecha", fecha)
                        .build(idVendedor))
                .retrieve()
                .body(TIPO_LISTA);
    }

    // Consultar ventas por rango de fechas
    public List<Map<String, Object>> listarVentasPorRango(
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ventas/rango")
                        .queryParam("fechaInicio", fechaInicio)
                        .queryParam("fechaFin", fechaFin)
                        .build())
                .retrieve()
                .body(TIPO_LISTA);
    }

    // Consultar ventas de un vendedor
    public List<Map<String, Object>> listarVentasPorVendedor(Long idVendedor) {

        return restClient.get()
                .uri("/api/ventas/vendedor/{idVendedor}", idVendedor)
                .retrieve()
                .body(TIPO_LISTA);
    }

    // Consultar ventas de un vendedor por rango de fechas
    public List<Map<String, Object>> listarVentasPorVendedorYRango(
            Long idVendedor,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/ventas/vendedor/{idVendedor}/rango")
                        .queryParam("fechaInicio", fechaInicio)
                        .queryParam("fechaFin", fechaFin)
                        .build(idVendedor))
                .retrieve()
                .body(TIPO_LISTA);
    }

    // Consultar vendedores para llenar el filtro
    public List<Map<String, Object>> listarVendedores() {

        return restClient.get()
                .uri("/api/vendedores")
                .retrieve()
                .body(TIPO_LISTA);
    }

    // Descargar reporte PDF diario conservando la compatibilidad anterior
    public byte[] descargarReportePdf(LocalDate fecha) {
        return descargarReportePdf(fecha, null, null);
    }

    // Descargar reporte PDF diario con filtros opcionales
    public byte[] descargarReportePdf(
            LocalDate fecha,
            String producto,
            Long idVendedor) {

        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/api/ventas/reporte/pdf")
                            .queryParam("fecha", fecha);

                    if (producto != null && !producto.isBlank()) {
                        uriBuilder.queryParam("producto", producto.trim());
                    }

                    if (idVendedor != null) {
                        uriBuilder.queryParam("idVendedor", idVendedor);
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(byte[].class);
    }

    // Descargar reporte PDF por rango de fechas con filtros opcionales
    public byte[] descargarReportePdfRango(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String producto,
            Long idVendedor) {

        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/api/ventas/reporte/rango/pdf")
                            .queryParam("fechaInicio", fechaInicio)
                            .queryParam("fechaFin", fechaFin);

                    if (producto != null && !producto.isBlank()) {
                        uriBuilder.queryParam("producto", producto.trim());
                    }

                    if (idVendedor != null) {
                        uriBuilder.queryParam("idVendedor", idVendedor);
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(byte[].class);
    }
}
