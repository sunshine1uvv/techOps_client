package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.services.EquipmentService;
import org.example.tech_ops_gui.synchronization.EquipmentSyncMessage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;

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
            if (items == null) return;

            for (EquipmentDto incomingItem : items) {
                switch (action) {
                    case "CREATE" -> {
                        if (equipmentList.stream().noneMatch(e -> e.getId().equals(incomingItem.getId()))) {
                            equipmentList.add(incomingItem);
                        }
                    }
                    case "UPDATE" -> {
                        for (int i = 0; i < equipmentList.size(); i++) {
                            if (equipmentList.get(i).getId().equals(incomingItem.getId())) {
                                equipmentList.set(i, incomingItem);
                                break;
                            }
                        }
                    }
                    case "DELETE" -> equipmentList.removeIf(item -> item.getId().equals(incomingItem.getId()));
                }
            }
        });
    }
}