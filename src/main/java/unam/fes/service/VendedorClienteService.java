
package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import unam.fes.model.SolicitudVendedorDTO;
import unam.fes.model.VendedorDTO;

import java.util.List;
import java.util.Map;

@Service
public class VendedorClienteService {

    private final RestClient restClient;

    public VendedorClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<VendedorDTO> listarVendedores() {
        List<VendedorDTO> vendedores = restClient.get()
                .uri("/api/vendedores")
                .retrieve()
                .body(new ParameterizedTypeReference<List<VendedorDTO>>() {});

        return vendedores != null ? vendedores : List.of();
    }

    public List<SolicitudVendedorDTO> listarSolicitudesPendientes() {
        List<SolicitudVendedorDTO> solicitudes = restClient.get()
                .uri("/api/vendedores/pendientes")
                .retrieve()
                .body(new ParameterizedTypeReference<List<SolicitudVendedorDTO>>() {});

        return solicitudes != null ? solicitudes : List.of();
    }

    public void registrarSolicitud(
            String nombre,
            String apellido,
            String telefono,
            Long idBloque,
            String username,
            String password) {

        restClient.post()
                .uri("/api/vendedores/registro")
                .body(Map.of(
                        "nombre", nombre,
                        "apellido", apellido,
                        "telefono", telefono,
                        "idBloque", idBloque,
                        "username", username,
                        "password", password
                ))
                .retrieve()
                .toBodilessEntity();
    }

    public void aprobarSolicitud(Long id) {
        restClient.put()
                .uri("/api/vendedores/{id}/aprobar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void rechazarSolicitud(Long id) {
        restClient.put()
                .uri("/api/vendedores/{id}/rechazar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void cambiarEstado(Long id, boolean activar) {
        restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/vendedores/{id}/estado")
                        .queryParam("activar", activar)
                        .build(id))
                .retrieve()
                .toBodilessEntity();
    }

    public void modificarVendedor(
            Long id,
            String nombre,
            String apellido,
            String telefono) {

        restClient.put()
                .uri("/api/vendedores/{id}", id)
                .body(Map.of(
                        "nombre", nombre,
                        "apellido", apellido,
                        "telefono", telefono
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
