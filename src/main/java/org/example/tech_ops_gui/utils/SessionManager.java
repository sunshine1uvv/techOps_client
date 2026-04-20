package org.example.tech_ops_gui.utils;

public class SessionManager {
    private static SessionManager instance;
    private String username;
    private String role;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public void clear() {
        this.username = null;
        this.role = null;
    }
}