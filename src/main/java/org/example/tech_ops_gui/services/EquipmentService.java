package org.example.tech_ops_gui.services;

import org.example.tech_ops_gui.api.ApiClient;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.OperatingHoursLogDto;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EquipmentService {
    private final ApiClient apiClient;

    public EquipmentService(ApiClient apiClient) {
        this.apiClient = apiClient;
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

    public CompletableFuture<Void> addOperatingHours(OperatingHoursLogDto logDto) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.postVoid("/equipment/hours", logDto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> deleteOperatingHours(Long logId) {
        return CompletableFuture.runAsync(() -> {
            try {
                apiClient.delete("/equipment/hours/" + logId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<OperatingHoursLogDto>> getHoursHistory(Long equipmentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                OperatingHoursLogDto[] arr = apiClient.get("/equipment/" + equipmentId + "/hours-history", OperatingHoursLogDto[].class);
                return Arrays.asList(arr);
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