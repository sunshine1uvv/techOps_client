package org.example.tech_ops_gui.controllers.admin;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.DepartmentDto;
import org.example.tech_ops_gui.repository.DepartmentRepository;
import org.example.tech_ops_gui.utils.NotificationManager;

public class DepartmentDeleteViewController {

    @FXML private ListView<DepartmentDto> departmentListView;
    @FXML private SearchableComboBox<DepartmentDto> departmentComboBox;

    private final DepartmentRepository departmentRepository = AppContext.getDepartmentRepository();

    private final ListChangeListener<DepartmentDto> repositoryListener = c -> {
        Platform.runLater(() -> {
            departmentListView.setItems(departmentRepository.getDepartmentsList());
            departmentComboBox.setItems(departmentRepository.getDepartmentsList());
        });
    };

    @FXML
    private void initialize() {
        departmentListView.setItems(departmentRepository.getDepartmentsList());
        departmentComboBox.setItems(departmentRepository.getDepartmentsList());


        departmentListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DepartmentDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        departmentComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(DepartmentDto object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public DepartmentDto fromString(String string) {
                return null;
            }
        });

        departmentListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Platform.runLater(() -> departmentComboBox.getSelectionModel().select(newVal));
            }
        });

        departmentRepository.getDepartmentsList().addListener(repositoryListener);
    }

    @FXML
    private void handleDelete() {
        DepartmentDto selectedDepartment = departmentComboBox.getValue();

        if (selectedDepartment == null || selectedDepartment.getId() == null) {
            NotificationManager.showError("Ошибка", "Пожалуйста, выберите подразделение для удаления.");
            return;
        }

        try {
            departmentRepository.delete(selectedDepartment.getId()).thenRun(() -> {
                Platform.runLater(() -> {
                    NotificationManager.showInfo("Успешно", "Подразделение [" + selectedDepartment.getName() + "] удалено.");
                    departmentComboBox.getSelectionModel().clearSelection();
                    closeWindow();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    NotificationManager.showError("Невозможно удалить", "Ошибка базы данных. Убедитесь, что в этом подразделении нет сотрудников и за ним не числится техника.");
                });
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        departmentRepository.getDepartmentsList().removeListener(repositoryListener);
        Stage stage = (Stage) departmentComboBox.getScene().getWindow();
        stage.close();
    }
}