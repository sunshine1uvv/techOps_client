package org.example.tech_ops_gui.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.tech_ops_gui.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String jwtToken;

    public ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void setJwtToken(String token) {
        this.jwtToken = token;
    }

    public String getJwtToken() {
        return this.jwtToken;
    }

    public void clearToken() {
        this.jwtToken = null;
    }

    private HttpRequest.Builder createBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.BASE_URL + path))
                .header("Content-Type", "application/json");

        if (jwtToken != null && !jwtToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        return builder;
    }


    public <T> T get(String path, Class<T> responseType) throws Exception {
        HttpRequest request = createBuilder(path).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return objectMapper.readValue(response.body(), responseType);
    }


    public <T, R> R post(String path, T body, Class<R> responseType) throws Exception {
        String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
        HttpRequest request = createBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return objectMapper.readValue(response.body(), responseType);
    }

    public <T> void postVoid(String path, T body) throws Exception {
        String jsonBody = body != null ? objectMapper.writeValueAsString(body) : "";
        HttpRequest request = createBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
    }

    public void delete(String path) throws Exception {
        HttpRequest request = createBuilder(path).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
    }

    private void checkStatus(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 300) {
            throw new RuntimeException("HTTP Error " + response.statusCode() + ": " + response.body());
        }
    }
}
