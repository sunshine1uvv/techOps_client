package org.example.tech_ops_gui.controllers.crud;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.repository.EquipmentTypeRepository;
import org.example.tech_ops_gui.utils.EquipmentValidator;
import org.example.tech_ops_gui.utils.FormatUtil;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.WindowManager;

import java.util.List;

public class EditViewController {

    @FXML private Label sideInvNumLabel;
    @FXML private TextField invNumField;
    @FXML private TextField serialNumField;
    @FXML private TextField nameField;
    @FXML private TextField locationField;

    // ВАЖНО: Изменен тип SearchableComboBox на UserDto, как в AddViewController
    @FXML private SearchableComboBox<EquipmentTypeDto> typeCombo;
    @FXML private SearchableComboBox<UserDto> employeeCombo;
    @FXML private ComboBox<String> categoryCombo;

    private final EquipmentDto selectedItem;
    private final EquipmentTypeRepository typeRepository = AppContext.getEquipmentTypeRepository();
    private final EquipmentRepository equipmentRepository = AppContext.getEquipmentRepository();

    public EditViewController(EquipmentDto selectedItem) {
        this.selectedItem = selectedItem;
    }

    @FXML
    public void initialize() {
        setupCategoryCombo();
        setupTypeCombo();
        setupEmployeeCombo();
        fillFieldsWithData();
    }

    private void setupCategoryCombo() {
        categoryCombo.getItems().addAll("1", "2", "3", "4", "5");
    }

    private void setupTypeCombo() {
        FilteredList<EquipmentTypeDto> level6Types = new FilteredList<>(typeRepository.getEquipmentTypesList());
        level6Types.setPredicate(type -> type.getLevel() != null && type.getLevel() == 6);

        typeCombo.setItems(level6Types);
        typeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(EquipmentTypeDto type) { return type == null ? "" : type.getName(); }
            @Override public EquipmentTypeDto fromString(String string) { return null; }
        });
    }

    private void setupEmployeeCombo() {
        // Теперь мы берем пользователей синхронно из репозитория, а не делаем лишний запрос к API
        employeeCombo.setItems(AppContext.getUserRepository().getUserList());
        employeeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(UserDto user) { return user == null ? "" : FormatUtil.buildFullName(user); }
            @Override public UserDto fromString(String string) { return null; }
        });
    }

    private void fillFieldsWithData() {
        if (selectedItem == null) return;

        invNumField.setText(selectedItem.getInventoryNumber());
        serialNumField.setText(selectedItem.getSerialNumber());
        nameField.setText(selectedItem.getName());
        locationField.setText(selectedItem.getLocation());
        sideInvNumLabel.setText(selectedItem.getInventoryNumber() != null ? selectedItem.getInventoryNumber() : "Новая запись");

        if (selectedItem.getCategory() != null) {
            categoryCombo.setValue(String.valueOf(selectedItem.getCategory()));
        }

        if (selectedItem.getType() != null && selectedItem.getType().getLevel() != null && selectedItem.getType().getLevel() == 6) {
            typeCombo.setValue(selectedItem.getType());
        }

        if (selectedItem.getEmployee() != null) {
            // Ищем точный объект сотрудника в списке, чтобы ComboBox его корректно выбрал
            Long empId = selectedItem.getEmployee().getId();
            employeeCombo.getItems().stream()
                    .filter(u -> u.getId().equals(empId))
                    .findFirst()
                    .ifPresent(u -> employeeCombo.setValue(u));
        }
    }

    @FXML
    private void handleEditClick(ActionEvent event) {
        EquipmentDto dto = buildEquipmentDtoFromForm();

        List<String> errors = EquipmentValidator.validate(dto);
        if (!errors.isEmpty()) {
            NotificationManager.showError("Ошибка заполнения", String.join("\n", errors));
            return;
        }

        equipmentRepository.save(dto)
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Успех", "Запись успешно обновлена.");
                    WindowManager.close(event); // Закрываем окно только при успехе!
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null; // Не закрываем окно при ошибке сервера
                });
    }

    private EquipmentDto buildEquipmentDtoFromForm() {
        EquipmentDto equipment = new EquipmentDto();
        equipment.setId(selectedItem.getId());
        equipment.setParent(selectedItem.getParent());
        equipment.setInventoryNumber(getSafeText(invNumField) != null ? getSafeText(invNumField).toUpperCase() : null);
        equipment.setSerialNumber(getSafeText(serialNumField));
        equipment.setName(getSafeText(nameField));
        equipment.setType(typeCombo.getValue());
        equipment.setCategory(categoryCombo.getValue() != null ? Integer.parseInt(categoryCombo.getValue()) : null);
        equipment.setLocation(getSafeText(locationField));
        equipment.setEmployee(employeeCombo.getValue()); // Теперь тут сразу берется объект UserDto
        return equipment;
    }

    private String getSafeText(TextField field) {
        return (field == null || field.getText() == null || field.getText().trim().isEmpty()) ? null : field.getText().trim();
    }

    @FXML
    private void handleCloseClick(ActionEvent event) {
        WindowManager.close(event);
    }
}