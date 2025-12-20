package com.example.ePolan.Services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class KeycloakClientService {

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;
    private final String tokenUri;
    private final String adminApiBase;

    public KeycloakClientService(
            @Value("${keycloak.client-id}") String clientId,
            @Value("${keycloak.client-secret}") String clientSecret,
            @Value("${keycloak.token-uri}") String tokenUri,
            @Value("${keycloak.admin-api-base}") String adminApiBase) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenUri = tokenUri;
        this.adminApiBase = adminApiBase;

        this.webClient = WebClient.builder().build();
    }

    private Mono<String> getAccessToken() {
        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> json.get("access_token").asText());
    }

    public Mono<String> getUserByEmail(String email) {
        return getAccessToken()
                .flatMap(token -> webClient.get()
                        .uri(adminApiBase + "/users?username=" + email)  // API w Keycloak do pobierania użytkownika
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)  // Wysłanie tokenu Bearer w nagłówku
                        .retrieve()
                        .bodyToMono(JsonNode.class)  // Pobranie odpowiedzi jako JsonNode
                        .flatMap(users -> {
                            // Zakładając, że odpowiedź to lista użytkowników, zwracamy pierwszego użytkownika
                            if (users.isArray() && users.size() > 0) {
                                JsonNode user = users.get(0);  // Zakładając, że zwraca to tablicę użytkowników
                                String firstName = user.path("firstName").asText("");  // Domyślnie pusty ciąg, jeśli brak
                                String lastName = user.path("lastName").asText("");  // Domyślnie pusty ciąg, jeśli brak
                                return Mono.just(firstName + " " + lastName);  // Łączenie imienia i nazwiska
                            } else {
                                return Mono.just("User not found");
                            }
                        })
                );
    }
}
