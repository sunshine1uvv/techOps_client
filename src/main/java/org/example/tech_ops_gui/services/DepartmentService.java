package org.example.tech_ops_gui.services;

import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.dto.DepartmentDto;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DepartmentService {

    private final ApiClient apiClient;

    public DepartmentService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<List<DepartmentDto>> getAllDepartments() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DepartmentDto[] arr = apiClient.get("/departments", DepartmentDto[].class);
                return Arrays.asList(arr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
