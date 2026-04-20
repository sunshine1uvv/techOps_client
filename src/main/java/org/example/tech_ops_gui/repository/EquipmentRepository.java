package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.services.EquipmentService;
import org.example.tech_ops_gui.synchronization.EquipmentSyncMessage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;
import org.example.tech_ops_gui.utils.WindowManager;

import java.util.List;

public class EquipmentRepository {

    private static EquipmentRepository instance;
    private final ObservableList<EquipmentDto> equipmentList = FXCollections.observableArrayList();
    private final WebSocketSyncClient syncClient = WebSocketSyncClient.getInstance();
    private final EquipmentService service = EquipmentService.getInstance();

    private EquipmentRepository() {
        syncClient.subscribeEquipment(this::handleSyncMessage);
    }

    public static EquipmentRepository getInstance() {
        if (instance == null) instance = new EquipmentRepository();
        return instance;
    }

    public void initData() {
        loadEquipmentFromServer();
    }

    private void loadEquipmentFromServer() {
        service.getAllEquipment().thenAccept(list ->
                Platform.runLater(() -> equipmentList.setAll(list))
        );
    }

    public ObservableList<EquipmentDto> getEquipmentList() {
        return equipmentList;
    }

    public void refresh() {
        loadEquipmentFromServer();
    }

    public void save(EquipmentDto equipment) {
        service.saveEquipment(equipment)
                .exceptionally(ex -> {
                    CustomExceptionHandler.handleError(ex);
                    return null;
                });
    }

    public void delete(Long id) {
        service.deleteEquipment(id)
                .exceptionally(ex -> {
                    CustomExceptionHandler.handleError(ex);
                    return null;
                });
    }

    public void detach(Long id) {
        service.detachEquipment(id)
                .exceptionally(ex -> {
                    CustomExceptionHandler.handleError(ex);
                    return null;
                });
    }

    public void attach(Long parentId, Long childId) {
        service.attachEquipment(parentId, childId)
                .exceptionally(ex -> {
                    CustomExceptionHandler.handleError(ex);
                    return null;
                });
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

//    public void cleanup() {
//        syncClient.unsubscribeEquipment(this::handleSyncMessage);
//    }


}
