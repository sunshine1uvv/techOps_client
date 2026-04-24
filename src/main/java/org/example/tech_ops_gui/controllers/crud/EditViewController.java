package org.example.tech_ops_gui.controllers.crud;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.entities.EquipmentType;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.repository.EquipmentTypeRepository;
import org.example.tech_ops_gui.services.UserService;
import org.example.tech_ops_gui.utils.WindowManager;

import java.util.List;

public class EditViewController {

    @FXML
    private Label sideInvNumLabel;
    @FXML
    private TextField invNumField;
    @FXML
    private TextField serialNumField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField employeeField;
    @FXML
    private SearchableComboBox<EquipmentType> typeCombo;
    @FXML
    private SearchableComboBox<String> employeeCombo;
    @FXML
    private ComboBox<String> categoryCombo;
    private final EquipmentDto selectedItem;
    private final EquipmentTypeRepository typeRepository = EquipmentTypeRepository.getInstance();
    private final EquipmentRepository equipmentRepository = EquipmentRepository.getInstance();
    private final UserService userService = UserService.getInstance();
    private List<UserDto> allUsers;

    public EditViewController(EquipmentDto selectedItem) {
        this.selectedItem = selectedItem;
    }

    @FXML
    public void initialize() {
        setupTypeCombo();
        setupEmployeeCombo();
        fillFieldsWithData();
    }


    private void setupTypeCombo() {
        ObservableList<EquipmentType> allTypes = typeRepository.getEquipmentTypesList();
        FilteredList<EquipmentType> level6Types = new FilteredList<>(allTypes);
        level6Types.setPredicate(type -> type.getLevel() != null && type.getLevel() == 6);

        typeCombo.setItems(level6Types);
        typeCombo.setConverter(new StringConverter<EquipmentType>() {
            @Override
            public String toString(EquipmentType type) {
                return type == null ? "" : type.getName();
            }
            @Override
            public EquipmentType fromString(String string) {
                return null; // не используется
            }
        });

        if (selectedItem != null && selectedItem.getType() != null) {
            EquipmentType currentType = selectedItem.getType();
            if (currentType.getLevel() != null && currentType.getLevel() == 6) {
                typeCombo.setValue(currentType);
            } else {
                typeCombo.setValue(null);
            }
        }
    }

    private void setupEmployeeCombo() {
        userService.getAllUsers().thenAccept(users -> Platform.runLater(() -> {
            allUsers = users;
            for (UserDto user : users) {
                String fullName = buildFullName(user);
                employeeCombo.getItems().add(fullName);
            }
            if (selectedItem != null && selectedItem.getEmployee() != null) {
                UserDto emp = selectedItem.getEmployee();
                String currentFullName = buildFullName(emp);
                employeeCombo.setValue(currentFullName);
            }
        })).exceptionally(ex -> {
            CustomExceptionHandler.handleError(ex);
            return null;
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
    }

    @FXML
    private void handleEditClick(ActionEvent event) {
        EquipmentType selectedType = typeCombo.getValue();
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

        EquipmentDto equipment = getEquipmentDto(selectedType, selectedUser);
        equipmentRepository.save(equipment);
    }

    private EquipmentDto getEquipmentDto(EquipmentType selectedType, UserDto selectedUser) {
        EquipmentDto equipment = new EquipmentDto();
        equipment.setId(selectedItem.getId());
        equipment.setParent(selectedItem.getParent());
        equipment.setInventoryNumber(invNumField.getText());
        equipment.setSerialNumber(serialNumField.getText());
        equipment.setName(nameField.getText());
        equipment.setType(selectedType);
        equipment.setCategory(Integer.parseInt(categoryCombo.getValue()));
        equipment.setLocation(locationField.getText());
        equipment.setEmployee(selectedUser);
        return equipment;
    }

    @FXML
    private void handleCloseClick(ActionEvent event) {
        WindowManager.close(event);
    }

    private String buildFullName(UserDto user) {
        return String.format("%s %s %s",
                user.getSurname(),
                user.getName(),
                user.getPatronymic() != null ? user.getPatronymic() : "").trim();
    }

}
