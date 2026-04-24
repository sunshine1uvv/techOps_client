package org.example.tech_ops_gui.controllers.crud;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.entities.EquipmentType;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.repository.EquipmentTypeRepository;
import org.example.tech_ops_gui.repository.UserRepository;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public class AddViewController {

    @FXML private SearchableComboBox<EquipmentType> typeCombo;
    @FXML private SearchableComboBox<UserDto> employeeCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField invNumField;
    @FXML private TextField serialNumField;
    @FXML private TextField nameField;
    @FXML private TextField locationField;

    @FXML private Label invStartLabel;
    @FXML private Label serialStartLabel;
    @FXML private TextField invStepField;
    @FXML private TextField serialStepField;
    @FXML private TextField countRowsField;
    @FXML private Button saveBtn;

    private final EquipmentRepository equipmentRepository = EquipmentRepository.getInstance();
    private final EquipmentTypeRepository typeRepository = EquipmentTypeRepository.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();

    @FXML
    public void initialize() {
        setupNumericFilters();
        setupCategoryCombo();
        setupTypeCombo();
        setupEmployeeCombo();
        setupMassAddLabels();
        bindFieldsToQuantity();
    }

    // Запрещаем ввод любых символов, кроме цифр, на уровне UI
    private void setupNumericFilters() {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d*")) return change;
            return null;
        };
        countRowsField.setTextFormatter(new TextFormatter<>(integerFilter));
        invStepField.setTextFormatter(new TextFormatter<>(integerFilter));
        serialStepField.setTextFormatter(new TextFormatter<>(integerFilter));
    }

    private void setupCategoryCombo() {
        categoryCombo.getItems().addAll("1", "2", "3", "4", "5");
    }

    private void setupTypeCombo() {
        FilteredList<EquipmentType> level6Types = new FilteredList<>(typeRepository.getEquipmentTypesList());
        level6Types.setPredicate(type -> type.getLevel() != null && type.getLevel() == 6);
        typeCombo.setItems(level6Types);
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(EquipmentType type) { return type == null ? "" : type.getName(); }
            @Override
            public EquipmentType fromString(String string) { return null; }
        });
    }

    private void setupEmployeeCombo() {
        employeeCombo.setItems(userRepository.getUserList());
        employeeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserDto user) { return user == null ? "" : buildFullName(user); }
            @Override
            public UserDto fromString(String string) { return null; }
        });
    }

    private void setupMassAddLabels() {
        invNumField.textProperty().addListener((obs, old, newVal) -> {
            String val = (newVal != null && !newVal.isBlank()) ? newVal.toUpperCase() : "не указан";
            invStartLabel.setText(val);
        });
        serialNumField.textProperty().addListener((obs, old, newVal) ->
                serialStartLabel.setText(newVal != null && !newVal.isBlank() ? newVal : "не указан"));
        invStartLabel.setText("не указан");
        serialStartLabel.setText("не указан");
    }

    private void bindFieldsToQuantity() {
        BooleanBinding isPositiveInt = Bindings.createBooleanBinding(() -> {
            String text = countRowsField.getText();
            return text != null && !text.isEmpty() && Integer.parseInt(text) > 0;
        }, countRowsField.textProperty());

        invStepField.disableProperty().bind(isPositiveInt.not().or(invNumField.textProperty().isEmpty()));
        serialStepField.disableProperty().bind(isPositiveInt.not().or(serialNumField.textProperty().isEmpty()));

        saveBtn.disableProperty().bind(
                countRowsField.textProperty().map(text -> text != null && !text.isEmpty() && Integer.parseInt(text) <= 0)
        );
    }

    @FXML
    private void handleSaveClick(ActionEvent event) {
        if (isMassAddMode()) {
            if (validateInventoryForMassAdd() && validateSerialForMassAdd()) {
                validateAndProcessBatch(generateFullBatch());
            }
        } else {
            processSingleAdd();
        }
    }

    private boolean isMassAddMode() {
        String qtyText = getSafeText(countRowsField);
        return qtyText != null && Integer.parseInt(qtyText) > 0;
    }

    private void processSingleAdd() {
        try {
            EquipmentDto equipment = buildEquipmentDtoFromForm();
            equipmentRepository.save(equipment);
            showInfo("Запись успешно добавлена.");
            clearFields();
        } catch (Exception e) {
            CustomExceptionHandler.handleError(e);
        }
    }

    /**
     * Основной метод маршрутизации. Он проверяет текущую партию на свежие конфликты.
     * Эту функцию можно вызывать повторно при серверных ошибках.
     */
    private void validateAndProcessBatch(List<EquipmentDto> batch) {
        Set<String> dbInvs = equipmentRepository.findAllInventoryNumbers();
        Set<String> dbSerials = equipmentRepository.findAllSerialNumbers();

        List<EquipmentDto> validItems = new ArrayList<>();
        List<EquipmentDto> conflictItems = new ArrayList<>();

        for (EquipmentDto item : batch) {
            boolean hasConflict = (item.getInventoryNumber() != null && dbInvs.contains(item.getInventoryNumber())) ||
                    (item.getSerialNumber() != null && dbSerials.contains(item.getSerialNumber()));
            if (hasConflict) conflictItems.add(item);
            else validItems.add(item);
        }

        if (conflictItems.isEmpty()) {
            sendBatchToServer(validItems);
        } else {
            openConflictDialog(validItems, conflictItems);
        }
    }

    private void openConflictDialog(List<EquipmentDto> valid, List<EquipmentDto> conflicts) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tech_ops_gui/fxml/conflict-resolver-view.fxml"));
            Parent root = loader.load();
            ConflictResolverController controller = loader.getController();

            // Передаем только конфликты. Свежие сеты контроллер возьмет сам при сохранении.
            controller.setData(conflicts);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Разрешение конфликтов");
            stage.showAndWait();

            if (controller.isConfirmed()) {
                List<EquipmentDto> finalBatch = new ArrayList<>(valid);
                finalBatch.addAll(controller.getResultList());

                // Важно: не отправляем слепо! Снова валидируем на случай,
                // если кто-то что-то добавил, пока окно было открыто.
                validateAndProcessBatch(finalBatch);
            }
        } catch (IOException e) {
            CustomExceptionHandler.handleError(e);
        }
    }

    private void sendBatchToServer(List<EquipmentDto> batch) {
        if (batch.isEmpty()) return;
        equipmentRepository.saveBatch(batch)
                .thenRun(() -> Platform.runLater(() -> {
                    showInfo("Успешно добавлено записей: " + batch.size());
                    clearMassAddFields();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        // Если сервер отклонил (например, гонка данных), уведомляем и рекурсивно проверяем
                        NotificationManager.showError("Ошибка сохранения", "Вероятно, записи были добавлены другим пользователем. Повторная проверка...");
                        validateAndProcessBatch(batch);
                    });
                    return null;
                });
    }

    private boolean validateInventoryForMassAdd() {
        String invStart = getSafeText(invNumField);
        if (invStart == null) return true;

        invStart = invStart.toUpperCase();
        if (!invStart.matches("^ИТ\\d{5}$")) {
            NotificationManager.showError("Ошибка", "Инвентарный номер должен быть в формате 'ИТ' и ровно 5 цифр.");
            return false;
        }

        int step = parseStep(invStepField, 1);
        int quantity = parseQuantity();
        int startNum = Integer.parseInt(invStart.substring(2));
        int lastNum = startNum + step * (quantity - 1);

        if (lastNum > 99999) {
            NotificationManager.showError("Ошибка", "Серия выходит за пределы ИТ99999. Уменьшите количество или шаг.");
            return false;
        }
        return true;
    }

    private boolean validateSerialForMassAdd() {
        String serStart = getSafeText(serialNumField);
        if (serStart == null) return true;
        if (!serStart.matches(".*\\d.*")) {
            NotificationManager.showError("Ошибка", "Серийный номер должен содержать хотя бы одну цифру.");
            return false;
        }
        return true;
    }

    private List<EquipmentDto> generateFullBatch() {
        int quantity = parseQuantity();
        String invStart = getSafeText(invNumField) != null ? getSafeText(invNumField).toUpperCase() : null;

        List<String> invs = generateSequence(invStart, quantity, parseStep(invStepField, 1), this::incrementInventoryNumber);
        List<String> serials = generateSequence(getSafeText(serialNumField), quantity, parseStep(serialStepField, 1), this::incrementSerialSuffix);

        List<EquipmentDto> batch = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            EquipmentDto dto = buildEquipmentDtoFromForm();
            dto.setInventoryNumber(invs.get(i));
            dto.setSerialNumber(serials.get(i));
            batch.add(dto);
        }
        return batch;
    }

    private EquipmentDto buildEquipmentDtoFromForm() {
        EquipmentDto dto = new EquipmentDto();
        dto.setType(typeCombo.getValue());
        dto.setName(getSafeText(nameField));
        dto.setInventoryNumber(getSafeText(invNumField) != null ? getSafeText(invNumField).toUpperCase() : null);
        dto.setSerialNumber(getSafeText(serialNumField));
        dto.setLocation(getSafeText(locationField));
        dto.setEmployee(employeeCombo.getValue());
        dto.setCategory(parseCategory());
        return dto;
    }

    // Вспомогательные методы
    private Integer parseCategory() {
        return categoryCombo.getValue() != null ? Integer.parseInt(categoryCombo.getValue()) : null;
    }

    private int parseStep(TextField field, int def) {
        String text = getSafeText(field);
        return text != null ? Integer.parseInt(text) : def;
    }

    private int parseQuantity() { return Integer.parseInt(getSafeText(countRowsField)); }

    private List<String> generateSequence(String start, int count, int step, java.util.function.BiFunction<String, Integer, String> inc) {
        List<String> res = new ArrayList<>(count);
        String current = start;
        for (int i = 0; i < count; i++) {
            res.add(current);
            if (current != null) current = inc.apply(current, step);
        }
        return res;
    }

    private String incrementInventoryNumber(String invNum, int step) {
        if (invNum == null || !invNum.matches("^ИТ\\d{5}$")) return invNum;
        int num = Integer.parseInt(invNum.substring(2));
        return String.format("ИТ%05d", num + step);
    }

    private String incrementSerialSuffix(String base, int step) {
        if (base == null || base.isEmpty()) return base;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(.*?)(\\d+)(\\D*)$").matcher(base);
        if (m.matches()) {
            return m.group(1) + String.format("%0" + m.group(2).length() + "d", Integer.parseInt(m.group(2)) + step) + m.group(3);
        }
        return base;
    }

    private void clearMassAddFields() {
        invStepField.setText("1");
        serialStepField.setText("1");
        countRowsField.clear();
    }

    private void clearFields() {
        invNumField.clear();
        serialNumField.clear();
        nameField.clear();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML private void handleCloseClick(ActionEvent event) { WindowManager.close(event); }

    private String getSafeText(TextField field) {
        if (field == null || field.getText() == null) return null;
        String text = field.getText().trim();
        return text.isEmpty() ? null : text;
    }

    private String buildFullName(UserDto user) {
        return String.format("%s %s %s", user.getSurname(), user.getName(), user.getPatronymic() != null ? user.getPatronymic() : "").trim();
    }
}