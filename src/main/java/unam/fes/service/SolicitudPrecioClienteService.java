
package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import unam.fes.model.SolicitudPrecioClienteDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class SolicitudPrecioClienteService {

    private final RestClient restClient;

    public SolicitudPrecioClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    // ==============================
    // ZONA DEL VENDEDOR
    // ==============================

    // Consultar historial de solicitudes propias
    public List<SolicitudPrecioClienteDTO> listarMisSolicitudes() {

        List<SolicitudPrecioClienteDTO> solicitudes = restClient.get()
                .uri("/api/solicitudes-precio/vendedor")
                .retrieve()
                .body(new ParameterizedTypeReference<List<SolicitudPrecioClienteDTO>>() {});

        return solicitudes != null ? solicitudes : List.of();
    }

    // Crear solicitud de cambio de precio
    public void crearSolicitud(
            Long idProductoVendedor,
            BigDecimal precioSolicitado) {

        Map<String, Object> solicitud = Map.of(
                "idProductoVendedor", idProductoVendedor,
                "precioSolicitado", precioSolicitado
        );

        restClient.post()
                .uri("/api/solicitudes-precio")
                .body(solicitud)
                .retrieve()
                .toBodilessEntity();
    }

    // ==============================
    // ZONA DEL ADMINISTRADOR
    // ==============================

    // Consultar solicitudes pendientes
    public List<SolicitudPrecioClienteDTO> listarPendientes() {

        List<SolicitudPrecioClienteDTO> solicitudes = restClient.get()
                .uri("/api/solicitudes-precio/pendientes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<SolicitudPrecioClienteDTO>>() {});

        return solicitudes != null ? solicitudes : List.of();
    }

    // Consultar todas las solicitudes
    public List<SolicitudPrecioClienteDTO> listarTodasLasSolicitudes() {

        List<SolicitudPrecioClienteDTO> solicitudes = restClient.get()
                .uri("/api/solicitudes-precio/todas")
                .retrieve()
                .body(new ParameterizedTypeReference<List<SolicitudPrecioClienteDTO>>() {});

        return solicitudes != null ? solicitudes : List.of();
    }

    // Autorizar solicitud
    public void autorizarSolicitud(Long idSolicitud, String observacion) {

        Map<String, String> datos = Map.of(
                "observacion",
                observacion == null ? "" : observacion
        );

        restClient.put()
                .uri("/api/solicitudes-precio/{id}/autorizar", idSolicitud)
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }

    // Rechazar solicitud
    public void rechazarSolicitud(Long idSolicitud, String observacion) {

        Map<String, String> datos = Map.of(
                "observacion",
                observacion == null ? "" : observacion
        );

        restClient.put()
                .uri("/api/solicitudes-precio/{id}/rechazar", idSolicitud)
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }
}
