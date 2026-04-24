package org.example.tech_ops_gui.services;

import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.dto.EquipmentDto;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EquipmentService {
    private final ApiClient apiClient = ApiClient.getInstance();
    private static final EquipmentService INSTANCE = new EquipmentService();

    private EquipmentService() {
    }

    public static EquipmentService getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<List<EquipmentDto>> getAllEquipment() {
        return CompletableFuture.supplyAsync(() -> fetchList("/equipment"));
    }

    public CompletableFuture<List<EquipmentDto>> getRootEquipment() {
        return CompletableFuture.supplyAsync(() -> fetchList("/equipment/roots"));
    }

    public CompletableFuture<Void> deleteEquipment(Long id) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.delete("/equipment/" + id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<EquipmentDto>> getBundleByParentId(Long parentId) {
        return CompletableFuture.supplyAsync(() -> fetchList("/equipment/bundle/" + parentId));
    }

    public CompletableFuture<List<EquipmentDto>> getEquipmentByParentId(Long parentId) {
        return CompletableFuture.supplyAsync(() -> fetchList("/equipment/parent/" + parentId));
    }

    public CompletableFuture<List<EquipmentDto>> getEquipmentByUserId(Long userId) {
        return CompletableFuture.supplyAsync(() -> fetchList("/equipment/user/" + userId));
    }

    public CompletableFuture<Void> saveEquipment(EquipmentDto equipment) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.postVoid("/equipment", equipment);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> attachEquipment(Long parentId, Long childId) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.postVoid("/equipment/attach?parent_id=" + parentId + "&child_id=" + childId, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> detachEquipment(Long childId) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.postVoid("/equipment/detach?child_id=" + childId, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> saveEquipmentBatch(List<EquipmentDto> batch) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.postVoid("/equipment/batch", batch);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<EquipmentDto> fetchList(String path) {
        try {
            EquipmentDto[] arr = apiClient.get(path, EquipmentDto[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<String>> getNextAvailableNumbers(int count) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = "/equipment/next-numbers?count=" + count;   // ← правильно
                String[] arr = apiClient.get(url, String[].class);
                return Arrays.asList(arr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}