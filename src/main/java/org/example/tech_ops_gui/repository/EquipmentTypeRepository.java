package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.services.EquipmentTypeService;
import org.example.tech_ops_gui.synchronization.EquipmentTypeSyncMessage;
import org.example.tech_ops_gui.synchronization.UserSyncMessage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EquipmentTypeRepository {

    private final ObservableList<EquipmentTypeDto> equipmentTypesList = FXCollections.observableArrayList();
    private final EquipmentTypeService service;
    private final WebSocketSyncClient syncClient;

    public EquipmentTypeRepository(EquipmentTypeService service,
                                   WebSocketSyncClient syncClient) {

        this.service = service;
        this.syncClient = syncClient;
        this.syncClient.subscribeEquipmentTypes(this::handleSyncMessage);
    }

    public void initData() {
        refresh();
    }

    public void refresh() {
        service.getAllTypes().thenAccept(list ->
                Platform.runLater(() -> equipmentTypesList.setAll(list))
        ).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public ObservableList<EquipmentTypeDto> getEquipmentTypesList() {
        return equipmentTypesList;
    }

    public CompletableFuture<Void> save(EquipmentTypeDto dto) {
       return service.save(dto);
    }

    public CompletableFuture<Void> delete(Long id) {
        return service.delete(id);
    }

    public void handleSyncMessage(EquipmentTypeSyncMessage message) {
        Platform.runLater(() -> {
            String action = message.getAction();
            List<EquipmentTypeDto> items = message.getPayload();
            if (items == null) return;
            for (EquipmentTypeDto incomingItem : items) {
                switch (action) {
                    case "CREATE" -> {
                        if (equipmentTypesList.stream().noneMatch(e -> e.getId().equals(incomingItem.getId()))) {
                            equipmentTypesList.add(incomingItem);
                        }
                    }
                    case "DELETE" -> equipmentTypesList.removeIf(item -> item.getId().equals(incomingItem.getId()));
                }
            }
        });
    }
}
