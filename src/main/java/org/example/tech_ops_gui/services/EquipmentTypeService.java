package org.example.tech_ops_gui.services;


import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EquipmentTypeService {
    private final ApiClient apiClient;

    public EquipmentTypeService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public CompletableFuture<List<EquipmentTypeDto>> getAllTypes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EquipmentTypeDto[] arr = apiClient.get("/equipment/types", EquipmentTypeDto[].class);
                return Arrays.asList(arr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<EquipmentTypeDto>> getAllByLevel(Integer level) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EquipmentTypeDto[] arr = apiClient.get("/equipment/types/level/" + level, EquipmentTypeDto[].class);
                return Arrays.asList(arr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
