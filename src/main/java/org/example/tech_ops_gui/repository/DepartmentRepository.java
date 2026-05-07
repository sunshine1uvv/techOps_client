package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.tech_ops_gui.dto.DepartmentDto;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.services.DepartmentService;
import org.example.tech_ops_gui.services.EquipmentTypeService;

public class DepartmentRepository {

    private final ObservableList<DepartmentDto> departmentsList = FXCollections.observableArrayList();

    private final DepartmentService service;

    public DepartmentRepository(DepartmentService service) {
        this.service = service;
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
}
