package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import unam.fes.model.BloqueVendedorDTO;
import unam.fes.model.ProductoDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class CatalogoVendedorClienteService {

    private final RestClient restClient;

    public CatalogoVendedorClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    // Obtener el bloque asignado al vendedor autenticado
    public BloqueVendedorDTO obtenerBloqueVendedor() {

        return restClient.get()
                .uri("/api/inventario/vendedor/bloque")
                .retrieve()
                .body(BloqueVendedorDTO.class);
    }

    // Consultar catálogo del bloque del vendedor
    public List<ProductoDTO> listarCatalogo(Long idBloque) {

        List<ProductoDTO> productos = restClient.get()
                .uri("/api/inventario/catalogo/bloque/{idBloque}", idBloque)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoDTO>>() {});

        return productos != null ? productos : List.of();
    }

    // Agregar producto al inventario del vendedor autenticado
    public void agregarProducto(Long idProducto, BigDecimal precio) {

        Map<String, Object> solicitud = Map.of(
                "idProducto", idProducto,
                "precio", precio
        );

        restClient.post()
                .uri("/api/inventario")
                .body(solicitud)
                .retrieve()
                .toBodilessEntity();
    }
}