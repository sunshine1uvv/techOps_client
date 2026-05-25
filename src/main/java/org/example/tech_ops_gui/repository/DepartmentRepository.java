package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.tech_ops_gui.dto.DepartmentDto;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.services.DepartmentService;
import org.example.tech_ops_gui.services.EquipmentTypeService;
import org.example.tech_ops_gui.synchronization.DepartmentSyncMessage;
import org.example.tech_ops_gui.synchronization.EquipmentTypeSyncMessage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DepartmentRepository {

    private final ObservableList<DepartmentDto> departmentsList = FXCollections.observableArrayList();

    private final DepartmentService service;

    private final WebSocketSyncClient syncClient;

    public DepartmentRepository(DepartmentService service,
                                WebSocketSyncClient syncClient) {
        this.service = service;
        this.syncClient = syncClient;
        this.syncClient.subscribeDepartments(this::handleSyncMessage);
    }

    public void initData() {
        refresh();
    }

    public void refresh() {
        service.getAllDepartments().thenAccept(list ->
                Platform.runLater(() -> departmentsList.setAll(list))
        ).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public ObservableList<DepartmentDto> getDepartmentsList() {
        return departmentsList;
    }

    public CompletableFuture<Void> save(DepartmentDto dto) {
        return service.save(dto);
    }

    public CompletableFuture<Void> delete(Long id) {
        return service.delete(id);
    }

    public void handleSyncMessage(DepartmentSyncMessage message) {
        Platform.runLater(() -> {
            String action = message.getAction();
            List<DepartmentDto> items = message.getPayload();
            if (items == null) return;
            for (DepartmentDto incomingItem : items) {
                switch (action) {
                    case "CREATE" -> {
                        if (departmentsList.stream().noneMatch(e -> e.getId().equals(incomingItem.getId()))) {
                            departmentsList.add(incomingItem);
                        }
                    }
                    case "DELETE" -> departmentsList.removeIf(item -> item.getId().equals(incomingItem.getId()));
                }
            }
        });
    }
}
