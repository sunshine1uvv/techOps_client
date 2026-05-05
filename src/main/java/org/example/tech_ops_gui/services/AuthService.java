package org.example.tech_ops_gui.services;


import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.JwtResponse;
import org.example.tech_ops_gui.dto.LoginRequest;
import org.example.tech_ops_gui.dto.RegistrationRequestDto;
import org.example.tech_ops_gui.utils.SessionManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthService {

    private final ApiClient apiClient;

    public AuthService(ApiClient apiClient) {
        this.apiClient=apiClient;
    }

    public CompletableFuture<JwtResponse> login(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LoginRequest req = new LoginRequest(username, password);
                JwtResponse resp = apiClient.post("/auth/login", req, JwtResponse.class);

                apiClient.setJwtToken(resp.getToken());
                AppContext.getSessionManager().setUsername(resp.getUsername());
                AppContext.getSessionManager().setRole(resp.getRole());

                return resp;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> register(RegistrationRequestDto dto) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.postVoid("/auth/register", dto);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
}
