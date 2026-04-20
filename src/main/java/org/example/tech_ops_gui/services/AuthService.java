package org.example.tech_ops_gui.services;


import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.dto.JwtResponse;
import org.example.tech_ops_gui.dto.LoginRequest;
import org.example.tech_ops_gui.dto.RegistrationRequestDto;
import org.example.tech_ops_gui.utils.SessionManager;

public class AuthService {

    private final ApiClient apiClient = ApiClient.getInstance();

    public JwtResponse login(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        JwtResponse resp = apiClient.post("/auth/login", req, JwtResponse.class);
        apiClient.setJwtToken(resp.getToken());
        SessionManager.getInstance().setUsername(resp.getUsername());
        SessionManager.getInstance().setRole(resp.getRole());

        return resp;
    }

    public void register(RegistrationRequestDto dto) throws Exception {
        apiClient.postVoid("/auth/register", dto);
    }
}
