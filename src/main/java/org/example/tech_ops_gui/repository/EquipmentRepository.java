package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.OperatingHoursLogDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.services.EquipmentService;
import org.example.tech_ops_gui.synchronization.EquipmentSyncMessage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EquipmentRepository {

    private final ObservableList<EquipmentDto> equipmentList = FXCollections.observableArrayList();
    private final EquipmentService service;
    private final WebSocketSyncClient syncClient;

    public EquipmentRepository(EquipmentService service, WebSocketSyncClient syncClient) {
        this.service = service;
        this.syncClient = syncClient;
        this.syncClient.subscribeEquipment(this::handleSyncMessage);
    }

    public void initData() {
        refresh();
    }

    public void refresh() {
        service.getAllEquipment().thenAccept(list ->
                Platform.runLater(() -> equipmentList.setAll(list))
        );
    }

    public ObservableList<EquipmentDto> getEquipmentList() {
        return equipmentList;
    }

    public CompletableFuture<Void> save(EquipmentDto equipment) {
        return service.saveEquipment(equipment);
    }

    public CompletableFuture<Void> saveBatch(List<EquipmentDto> batch) {
        return service.saveEquipmentBatch(batch);
    }

    public CompletableFuture<Void> delete(Long id) {
        return service.deleteEquipment(id);
    }

    public CompletableFuture<Void> detach(Long id) {
        return service.detachEquipment(id);
    }

    public CompletableFuture<Void> attach(Long parentId, Long childId) {
        return service.attachEquipment(parentId, childId);
    }

    public CompletableFuture<Void> addOperatingHours(OperatingHoursLogDto logDto) {
        return service.addOperatingHours(logDto);
    }

    public CompletableFuture<Void> deleteOperatingHours(Long logId) {
        return service.deleteOperatingHours(logId);
    }

    public CompletableFuture<List<OperatingHoursLogDto>> getHoursHistory(Long equipmentId) {
        return service.getHoursHistory(equipmentId);
    }


    public Set<String> findAllInventoryNumbers() {
        return equipmentList.stream()
                .map(EquipmentDto::getInventoryNumber)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public Set<String> findAllSerialNumbers() {
        return equipmentList.stream()
                .map(EquipmentDto::getSerialNumber)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public CompletableFuture<List<String>> getNextAvailableInventoryNumbers(int count) {
        return service.getNextAvailableNumbers(count);
    }

    private void handleSyncMessage(EquipmentSyncMessage message) {
        Platform.runLater(() -> {
            String action = message.getAction();
            List<EquipmentDto> items = message.getPayload();
            if (items == null || items.isEmpty()) return;

            switch (action) {
                case "CREATE" -> {
                    List<EquipmentDto> toAdd = new ArrayList<>();
                    for (EquipmentDto incoming : items) {
                        if (equipmentList.stream().noneMatch(e -> e.getId().equals(incoming.getId()))) {
                            toAdd.add(incoming);
                        }
                    }
                    if (!toAdd.isEmpty()) {
                        equipmentList.addAll(toAdd); // Одно событие на весь список
                    }
                }
                case "UPDATE" -> {
                    for (EquipmentDto incoming : items) {
                        for (int i = 0; i < equipmentList.size(); i++) {
                            if (equipmentList.get(i).getId().equals(incoming.getId())) {
                                equipmentList.set(i, incoming);
                                break;
                            }
                        }
                    }
                }
                case "DELETE" -> {
                    Set<Long> idsToRemove = items.stream().map(EquipmentDto::getId).collect(Collectors.toSet());
                    equipmentList.removeIf(item -> idsToRemove.contains(item.getId()));
                }
            }
        });
    }
}