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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.services.EquipmentBatchService;
import org.example.tech_ops_gui.utils.EquipmentValidator;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class AddViewController {

    @FXML private SearchableComboBox<EquipmentTypeDto> typeCombo;
    @FXML private SearchableComboBox<UserDto> employeeCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField invNumField, serialNumField, nameField, locationField;
    @FXML private Label invStartLabel, serialStartLabel;
    @FXML private TextField invStepField, serialStepField, countRowsField;
    @FXML private Button saveBtn;

    private final EquipmentBatchService batchService = AppContext.getEquipmentBatchService();

    @FXML
    public void initialize() {
        setupNumericFilters();
        setupCategoryCombo();
        setupTypeCombo();
        setupEmployeeCombo();
        setupMassAddLabels();
        bindFieldsToQuantity();
    }

    // =================================================================================================
    // 1. ОСНОВНОЙ ПАЙПЛАЙН СОХРАНЕНИЯ (ЕДИНАЯ ТОЧКА ВХОДА)
    // =================================================================================================

    @FXML
    private void handleSaveClick(ActionEvent event) {
        // Шаг 1: Собираем базовый DTO из формы
        EquipmentDto baseDto = buildEquipmentDtoFromForm();

        // Шаг 2: Проверяем базовые поля (Тип, Категория, Локация, формат начальных номеров)
        List<String> errors = EquipmentValidator.validate(baseDto);
        if (!errors.isEmpty()) {
            NotificationManager.showError("Ошибка заполнения", String.join("\n", errors));
            return;
        }

        // Шаг 3: Маршрутизация (Массовое или Одиночное)
        if (isMassAddMode()) {
            processMassAdd(baseDto);
        } else {
            processSingleAdd(baseDto);
        }
    }

    // =================================================================================================
    // 2. ЛОГИКА ДОБАВЛЕНИЯ
    // =================================================================================================

    private void processSingleAdd(EquipmentDto dto) {
        if (batchService.hasDatabaseConflict(dto)) {
            NotificationManager.showError("Конфликт данных", "Оборудование с таким Инвентарным или Серийным номером уже существует.");
            return;
        }

        // Ждем ответа от сервера асинхронно
        AppContext.getEquipmentRepository().save(dto)
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Успех", "Запись успешно добавлена.");
                    closeWindow(); // Закрываем окно только при успешном сохранении!
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null; // При ошибке окно остается открытым
                });
    }

    private void processMassAdd(EquipmentDto baseDto) {
        String invStart = baseDto.getInventoryNumber();
        String serialStart = baseDto.getSerialNumber();
        int invStep = parseStep(invStepField);
        int serialStep = parseStep(serialStepField);
        int qty = parseQuantity();

        // Проверяем, не выходит ли серия за рамки (напр. ИТ99999)
        String massAddError = batchService.validateMassAddParams(invStart, invStep, serialStart, qty);
        if (massAddError != null) {
            NotificationManager.showError("Ошибка генерации", massAddError);
            return;
        }

        // Генерируем партию
        List<EquipmentDto> batch = batchService.generateBatch(baseDto, invStart, invStep, serialStart, serialStep, qty);
        validateAndProcessBatch(batch);
    }

    // =================================================================================================
    // 3. РАБОТА С ПАРТИЯМИ И КОНФЛИКТАМИ (МАССОВОЕ ДОБАВЛЕНИЕ)
    // =================================================================================================

    private void validateAndProcessBatch(List<EquipmentDto> batch) {
        List<EquipmentDto> validItems = new ArrayList<>();
        List<EquipmentDto> conflictItems = new ArrayList<>();

        for (EquipmentDto item : batch) {
            if (batchService.hasDatabaseConflict(item)) conflictItems.add(item);
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
            controller.setData(conflicts);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Разрешение конфликтов");
            stage.showAndWait();

            if (controller.isConfirmed()) {
                List<EquipmentDto> finalBatch = new ArrayList<>(valid);
                finalBatch.addAll(controller.getResultList());
                validateAndProcessBatch(finalBatch);
            }
        } catch (IOException e) {
            CustomExceptionHandler.handleError(e);
        }
    }

    private void sendBatchToServer(List<EquipmentDto> batch) {
        if (batch.isEmpty()) return;

        AppContext.getEquipmentRepository().saveBatch(batch)
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Успех", "Успешно добавлено записей: " + batch.size());
                    closeWindow(); // Закрываем окно после успешного сохранения партии!
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        NotificationManager.showError("Ошибка", "Вероятно, записи были добавлены другим пользователем. Проверяем заново...");
                        validateAndProcessBatch(batch);
                    });
                    return null;
                });
    }

    // =================================================================================================
    // 4. ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ И ИНИЦИАЛИЗАЦИЯ
    // =================================================================================================

    private void closeWindow() {
        if (saveBtn != null && saveBtn.getScene() != null) {
            Stage stage = (Stage) saveBtn.getScene().getWindow();
            stage.close();
        }
    }

    private EquipmentDto buildEquipmentDtoFromForm() {
        EquipmentDto dto = new EquipmentDto();
        dto.setType(typeCombo.getValue());
        dto.setName(getSafeText(nameField));
        dto.setInventoryNumber(getSafeText(invNumField) != null ? getSafeText(invNumField).toUpperCase() : null);
        dto.setSerialNumber(getSafeText(serialNumField));
        dto.setLocation(getSafeText(locationField));
        dto.setEmployee(employeeCombo.getValue());
        dto.setCategory(categoryCombo.getValue() != null ? Integer.parseInt(categoryCombo.getValue()) : null);
        return dto;
    }

    private boolean isMassAddMode() {
        String qtyText = getSafeText(countRowsField);
        return qtyText != null && Integer.parseInt(qtyText) > 0;
    }

    private int parseStep(TextField field) {
        String text = getSafeText(field);
        return text != null ? Integer.parseInt(text) : 1;
    }

    private int parseQuantity() {
        return Integer.parseInt(getSafeText(countRowsField));
    }

    private String getSafeText(TextField field) {
        return (field == null || field.getText() == null || field.getText().trim().isEmpty()) ? null : field.getText().trim();
    }

    private void setupNumericFilters() {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> change.getControlNewText().matches("\\d*") ? change : null;
        countRowsField.setTextFormatter(new TextFormatter<>(integerFilter));
        invStepField.setTextFormatter(new TextFormatter<>(integerFilter));
        serialStepField.setTextFormatter(new TextFormatter<>(integerFilter));
    }

    private void setupCategoryCombo() {
        categoryCombo.getItems().addAll("1", "2", "3", "4", "5");
    }

    private void setupTypeCombo() {
        FilteredList<EquipmentTypeDto> level6Types = new FilteredList<>(AppContext.getEquipmentTypeRepository().getEquipmentTypesList());
        level6Types.setPredicate(type -> type.getLevel() != null && type.getLevel() == 6);
        typeCombo.setItems(level6Types);
        typeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(EquipmentTypeDto type) { return type == null ? "" : type.getName(); }
            @Override public EquipmentTypeDto fromString(String string) { return null; }
        });
    }

    private void setupEmployeeCombo() {
        employeeCombo.setItems(AppContext.getUserRepository().getUserList());
        employeeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(UserDto u) { return u == null ? "" : u.getSurname() + " " + u.getName(); }
            @Override public UserDto fromString(String string) { return null; }
        });
    }

    private void setupMassAddLabels() {
        invNumField.textProperty().addListener((obs, old, newVal) -> invStartLabel.setText((newVal != null && !newVal.isBlank()) ? newVal.toUpperCase() : "не указан"));
        serialNumField.textProperty().addListener((obs, old, newVal) -> serialStartLabel.setText((newVal != null && !newVal.isBlank()) ? newVal : "не указан"));
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
        saveBtn.disableProperty().bind(countRowsField.textProperty().map(text -> text != null && !text.isEmpty() && Integer.parseInt(text) <= 0));
    }

    @FXML
    private void handleCloseClick(ActionEvent event) {
        WindowManager.close(event);
    }
}