package unam.fes.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import unam.fes.model.LoginRequestDTO;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AuthClienteService {

    private final RestClient restClient;

    public AuthClienteService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String autenticar(LoginRequestDTO login, HttpSession session) {

        try {
            String credentials = login.getUsername() + ":" + login.getPassword();

            String encodedAuth = Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            String authorization = "Basic " + encodedAuth;

            session.setAttribute("apiAuthorization", authorization);

            // Comprobar si es vendedor
            try {
                restClient.get()
                        .uri("/api/inventario/vendedor")
                        .retrieve()
                        .toBodilessEntity();

                return "VENDEDOR";

            } catch (HttpClientErrorException.Forbidden e) {

                // Si no es vendedor, comprobar si es administrador
                restClient.get()
                        .uri("/api/ventas")
                        .retrieve()
                        .toBodilessEntity();

                return "ADMINISTRADOR";
            }

        } catch (RestClientException e) {

            session.removeAttribute("apiAuthorization");

            return null;
        }
    }
}