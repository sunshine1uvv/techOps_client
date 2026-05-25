package org.example.tech_ops_gui.controllers.admin;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.DepartmentDto;
import org.example.tech_ops_gui.repository.DepartmentRepository;
import org.example.tech_ops_gui.utils.DepartmentValidator;
import org.example.tech_ops_gui.utils.NotificationManager;

import java.util.List;

public class DepartmentAddViewController {

    @FXML private TableView<DepartmentDto> departmentTable;
    @FXML private TableColumn<DepartmentDto, String> nameCol;
    @FXML private TableColumn<DepartmentDto, String> descCol;

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;

    private final DepartmentRepository departmentRepository = AppContext.getDepartmentRepository();

    @FXML
    private void initialize() {
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        descCol.setCellValueFactory(cellData -> {
            String desc = cellData.getValue().getDescription();
            return new SimpleStringProperty(desc != null ? desc : "");
        });

        departmentTable.setItems(departmentRepository.getDepartmentsList());
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText();
        String description = descriptionArea.getText();

        DepartmentDto newDepartment = new DepartmentDto();
        newDepartment.setName(name == null ? "" : name.trim());
        newDepartment.setDescription(description == null ? "" : description.trim());

        List<String> validationErrors = DepartmentValidator.validate(newDepartment);
        if (!validationErrors.isEmpty()) {
            String errorMessage = String.join("\n", validationErrors);
            NotificationManager.showError("Ошибка валидации", errorMessage);
            return;
        }

        try {
            departmentRepository.save(newDepartment).thenRun(() -> {
                Platform.runLater(() -> {
                    departmentRepository.refresh();
                    NotificationManager.showInfo("Успешно", "Подразделение [" + newDepartment.getName() + "] добавлено.");
                    closeWindow();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    NotificationManager.showError("Ошибка сервера", "Не удалось сохранить подразделение. Возможно, такое имя уже существует.");
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
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}