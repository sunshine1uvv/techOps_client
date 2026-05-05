package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.services.EquipmentTypeService;

public class EquipmentTypeRepository {

    private final ObservableList<EquipmentTypeDto> equipmentTypesList = FXCollections.observableArrayList();
    private final EquipmentTypeService service;

    public EquipmentTypeRepository(EquipmentTypeService service) {
        this.service = service;
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
}
