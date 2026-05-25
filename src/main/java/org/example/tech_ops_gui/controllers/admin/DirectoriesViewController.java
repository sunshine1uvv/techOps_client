package org.example.tech_ops_gui.controllers.admin;

import javafx.fxml.FXML;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.WindowManager;

public class DirectoriesViewController {
    @FXML
    private void handleAddType() {
        WindowManager.openModalWindow("admin/type-add-view.fxml", "Добавление типа оборудования");
    }

    @FXML
    private void handleDeleteType() {
        WindowManager.openModalWindow("admin/type-delete-view.fxml", "Удаление типа оборудования");
    }

    @FXML
    private void handleAddDepartment() {
        WindowManager.openModalWindow("admin/department-add-view.fxml", "Добавление подразделения");
    }

    @FXML
    private void handleDeleteDepartment() {
        WindowManager.openModalWindow("admin/department-delete-view.fxml", "Удаление подразделения");
    }
}
