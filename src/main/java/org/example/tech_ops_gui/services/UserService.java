package org.example.tech_ops_gui.services;

import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.entities.User;
import org.example.tech_ops_gui.entities.UserRole;
import org.example.tech_ops_gui.entities.UserStatus;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserService {

    private final ApiClient apiClient = ApiClient.getInstance();
    private static final UserService INSTANCE = new UserService();

    private UserService() {}

    public static UserService getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<List<UserDto>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UserDto[] arr = apiClient.get("/users", UserDto[].class);
                return Arrays.asList(arr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<UserDto> getByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UserDto user = apiClient.get("/users?username=" + username, UserDto.class);
                return user;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> updateUserStatus(Long userId, UserStatus newStatus) {
        return CompletableFuture.runAsync(() -> {
            try {
                String path = String.format("/users/status?user_id=%d&status=%s", userId, newStatus.name());
                apiClient.postVoid(path, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> updateUserRole(Long userId, UserRole newRole) {
        return CompletableFuture.runAsync(() -> {
            try {
                String path = String.format("/users/role?user_id=%d&role=%s", userId, newRole.name());
                apiClient.postVoid(path, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> deleteUser(Long userId) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.delete("/users/" + userId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}