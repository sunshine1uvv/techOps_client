package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.tech_ops_gui.entities.EquipmentType;
import org.example.tech_ops_gui.services.EquipmentTypeService;

public class EquipmentTypeRepository {

    private static EquipmentTypeRepository instance;

    private final EquipmentTypeService service = new EquipmentTypeService();
    private final ObservableList<EquipmentType> equipmentTypesList = FXCollections.observableArrayList();

    private EquipmentTypeRepository() {
        loadEquipmentTypesFromServer();
    }

    public static EquipmentTypeRepository getInstance() {
        if (instance == null) instance = new EquipmentTypeRepository();
        return instance;
    }

    private void loadEquipmentTypesFromServer() {
        service.getAllTypes().thenAccept(list -> {
            Platform.runLater(() -> equipmentTypesList.setAll(list));
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public ObservableList<EquipmentType> getEquipmentTypesList() {
        return equipmentTypesList;
    }
}
