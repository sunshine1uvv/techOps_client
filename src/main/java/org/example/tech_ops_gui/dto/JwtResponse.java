package org.example.tech_ops_gui.dto;

public class JwtResponse {
    private String token;
    private String username;
    private String type = "Bearer";
    private String role;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}