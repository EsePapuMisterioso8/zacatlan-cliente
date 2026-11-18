package unam.fes.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
public class RestClientConfig {

    @Value("${api.base-url}")
    private String apiBaseUrl;

    @Bean
    @SessionScope
    public RestClient restClient(HttpSession session) {

        return RestClient.builder()
                .baseUrl(apiBaseUrl)
                .requestInterceptor((request, body, execution) -> {

                    String authorization =
                            (String) session.getAttribute("apiAuthorization");

                    if (authorization != null) {
                        request.getHeaders().set(
                                HttpHeaders.AUTHORIZATION,
                                authorization
                        );
                    }

                    return execution.execute(request, body);
                })
                .build();
    }
}