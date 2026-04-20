package org.example.tech_ops_gui.controllers.crud;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.entities.Equipment;
import org.example.tech_ops_gui.entities.EquipmentType;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.repository.EquipmentTypeRepository;
import org.example.tech_ops_gui.services.EquipmentService;
import org.example.tech_ops_gui.services.UserService;
import org.example.tech_ops_gui.utils.WindowManager;

import java.util.List;

public class AddViewController {

    @FXML
    private SearchableComboBox<EquipmentType> typeCombo;
    @FXML
    private SearchableComboBox<String> employeeCombo;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private TextField invNumField;
    @FXML
    private TextField serialNumField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;

    private final EquipmentRepository equipmentRepository = EquipmentRepository.getInstance();
    private final EquipmentTypeRepository typeRepository = EquipmentTypeRepository.getInstance();
    private final UserService userService = UserService.getInstance();
    private List<UserDto> allUsers;

    @FXML
    public void initialize() {
        setupCategoryCombo();
        setupTypeCombo();
        setupEmployeeCombo();
    }

    /**
     * Заполнение списка категорий (1-5)
     */
    private void setupCategoryCombo() {
        categoryCombo.getItems().addAll("1", "2", "3", "4", "5");
    }

    /**
     * Загрузка типов оборудования через репозиторий
     */
    private void setupTypeCombo() {
        FilteredList<EquipmentType> level6Types = new FilteredList<>(typeRepository.getEquipmentTypesList());
        level6Types.setPredicate(type -> type.getLevel() != null && type.getLevel() == 6);
        typeCombo.setItems(level6Types);
        typeCombo.setConverter(new StringConverter<EquipmentType>() {
            @Override
            public String toString(EquipmentType type) {
                return type == null ? "" : type.getName();
            }

            @Override
            public EquipmentType fromString(String string) {
                return null;
            }
        });
    }

    private void setupEmployeeCombo() {
        userService.getAllUsers().thenAccept(users -> Platform.runLater(() -> {
            allUsers = users;
            for (UserDto user : users) {
                String fullName = buildFullName(user);
                employeeCombo.getItems().add(fullName);
            }
        })).exceptionally(ex -> {
            CustomExceptionHandler.handleError(ex);
            return null;
        });
    }

    @FXML
    private void handleSaveClick(ActionEvent event) {
        try {
            EquipmentType selectedType = typeCombo.getValue();
            if (selectedType == null) {
                throw new RuntimeException("Необходимо выбрать тип оборудования");
            }
            EquipmentDto equipment = new EquipmentDto();
            equipment.setType(selectedType);
            equipment.setName(getSafeText(nameField));
            equipment.setInventoryNumber(getSafeText(invNumField));
            equipment.setSerialNumber(getSafeText(serialNumField));
            equipment.setLocation(getSafeText(locationField));
            UserDto selectedUser = null;
            String selectedFullName = employeeCombo.getValue();
            if (selectedFullName != null && !selectedFullName.isBlank()) {
                for (UserDto user : allUsers) {
                    if (buildFullName(user).equals(selectedFullName)) {
                        selectedUser = user;
                        break;
                    }
                }
            }
            equipment.setEmployee(selectedUser);
            if (categoryCombo.getValue() != null) {
                equipment.setCategory(Integer.parseInt(categoryCombo.getValue()));
            }
            equipmentRepository.save(equipment);
        } catch (Exception e) {
            CustomExceptionHandler.handleError(e);
        }
    }

    @FXML
    private void handleCloseClick(ActionEvent event) {
        WindowManager.close(event);
    }

    private String getSafeText(TextField field) {
        if (field == null || field.getText() == null) return null;
        String text = field.getText().trim();
        return text.isEmpty() ? null : text;
    }

    private String buildFullName(UserDto user) {
        return String.format("%s %s %s",
                user.getSurname(),
                user.getName(),
                user.getPatronymic() != null ? user.getPatronymic() : "").trim();
    }
}
