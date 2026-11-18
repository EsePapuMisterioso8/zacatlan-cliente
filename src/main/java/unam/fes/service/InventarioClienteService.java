package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import unam.fes.model.InventarioVendedorDTO;

import java.util.List;

@Service
public class InventarioClienteService {

    private final RestClient restClient;

    public InventarioClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<InventarioVendedorDTO> listarInventarioPropio() {

        List<InventarioVendedorDTO> inventario = restClient.get()
                .uri("/api/inventario/vendedor")
                .retrieve()
                .body(new ParameterizedTypeReference<List<InventarioVendedorDTO>>() {});

        return inventario != null ? inventario : List.of();
    }
}