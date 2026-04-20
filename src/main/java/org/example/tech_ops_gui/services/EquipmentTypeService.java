package org.example.tech_ops_gui.services;


import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.entities.EquipmentType;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EquipmentTypeService {
    private final ApiClient apiClient = ApiClient.getInstance();

    public CompletableFuture<List<EquipmentType>> getAllTypes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EquipmentType[] arr = apiClient.get("/equipment/types", EquipmentType[].class);
                return Arrays.asList(arr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<EquipmentType>> getAllByLevel(Integer level) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EquipmentType[] arr = apiClient.get("/equipment/types/level/" + level, EquipmentType[].class);
                return Arrays.asList(arr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
