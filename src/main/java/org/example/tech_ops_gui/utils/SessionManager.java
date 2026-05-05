package org.example.tech_ops_gui.utils;

import org.example.tech_ops_gui.enums.UserRole;

public class SessionManager {
    private static SessionManager instance;
    private String username;
    private UserRole role;

    public SessionManager() {
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(String role) {
        if (role != null) {
            this.role = UserRole.valueOf(role);
        }
    }

    public void clear() {
        this.username = null;
        this.role = null;
    }
}