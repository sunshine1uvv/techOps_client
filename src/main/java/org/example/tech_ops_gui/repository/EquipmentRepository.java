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

    private static EquipmentRepository instance;
    private final ObservableList<EquipmentDto> equipmentList = FXCollections.observableArrayList();
    private final WebSocketSyncClient syncClient = WebSocketSyncClient.getInstance();
    private final EquipmentService service = EquipmentService.getInstance();

    private EquipmentRepository() {
        syncClient.subscribeEquipment(this::handleSyncMessage);
    }

    public static synchronized EquipmentRepository getInstance() {
        if (instance == null) instance = new EquipmentRepository();
        return instance;
    }

    public void initData() { refresh(); }

    public void refresh() {
        service.getAllEquipment().thenAccept(list ->
                Platform.runLater(() -> equipmentList.setAll(list))
        );
    }

    public ObservableList<EquipmentDto> getEquipmentList() {
        return equipmentList;
    }

    public void save(EquipmentDto equipment) {
        service.saveEquipment(equipment).exceptionally(ex -> {
            CustomExceptionHandler.handleError(ex);
            return null;
        });
    }

    public CompletableFuture<Void> saveBatch(List<EquipmentDto> batch) {
        return service.saveEquipmentBatch(batch);
    }

    public void delete(Long id) {
        service.deleteEquipment(id).exceptionally(ex -> {
            CustomExceptionHandler.handleError(ex);
            return null;
        });
    }

    public void detach(Long id) {
        service.detachEquipment(id).exceptionally(ex -> {
            CustomExceptionHandler.handleError(ex);
            return null;
        });
    }

    public void attach(Long parentId, Long childId) {
        service.attachEquipment(parentId, childId).exceptionally(ex -> {
            CustomExceptionHandler.handleError(ex);
            return null;
        });
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