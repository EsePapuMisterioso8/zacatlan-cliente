package unam.fes.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import unam.fes.model.ProductoDTO;
import java.util.List;

@Service
public class ProductoClienteService {

    private final RestClient restClient;

    // Se inyecta automáticamente el RestClient configurado con seguridad
    public ProductoClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<ProductoDTO> obtenerTodos() {
        return restClient.get()
                .uri("/api/productos") // Ajusta la ruta según tu BloqueRestController/ProductoRestController
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoDTO>>() {});
    }
}

/*package unam.fes.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import unam.fes.model.ProductoDTO;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
public class ProductoClienteService {
    private final RestClient restClient = RestClient.builder()
            .baseUrl("http://localhost:8080/api")
            .build();

    //Metodo para listar los productos
    public List<ProductoDTO> listarProductos(String username, String password){
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);

        List<ProductoDTO> productos = restClient.get()
                .uri("/productos")
                .header(HttpHeaders.AUTHORIZATION, authHeader) // <--- ¡Esta es la llave!
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoDTO>>() {});

        return productos;
    }
}*/
