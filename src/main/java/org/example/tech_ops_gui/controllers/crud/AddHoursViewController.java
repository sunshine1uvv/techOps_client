package org.example.tech_ops_gui.controllers.crud;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.OperatingHoursLogDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.WindowManager;

import java.util.function.UnaryOperator;

public class AddHoursViewController {

    @FXML private Label typeLabel;
    @FXML private Label nameLabel;
    @FXML private Label invLabel;
    @FXML private TextField hoursField;

    private final EquipmentRepository equipmentRepository = AppContext.getEquipmentRepository();
    private final EquipmentDto selectedItem;

    public AddHoursViewController(EquipmentDto selectedItem) {
        this.selectedItem = selectedItem;
    }

    @FXML
    public void initialize() {
        String type = (selectedItem.getType() != null && !selectedItem.getType().getName().trim().isEmpty())
                ? selectedItem.getType().getName()
                : "Тип не указан";
        typeLabel.setText(type);

        String name = selectedItem.getName();
        if (name != null && !name.trim().isEmpty()) {
            nameLabel.setText(name);
            nameLabel.setVisible(true);
            nameLabel.setManaged(true);
        } else {
            nameLabel.setVisible(false);
            nameLabel.setManaged(false);
        }

        invLabel.setText("Инв. №: " + selectedItem.getInventoryNumber());

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };
        hoursField.setTextFormatter(new TextFormatter<>(integerFilter));
    }

    @FXML
    private void handleSave(ActionEvent event) {
        // Читаем и проверяем данные из TextField
        String hoursText = hoursField.getText();
        if (hoursText == null || hoursText.isEmpty()) {
            NotificationManager.showError("Ошибка", "Введите количество часов.");
            return;
        }

        int hoursToAdd = Integer.parseInt(hoursText);
        if (hoursToAdd <= 0) {
            NotificationManager.showError("Ошибка", "Количество часов должно быть больше 0.");
            return;
        }

        String currentUsername = AppContext.getSessionManager().getUsername();
        UserDto currentUser = AppContext.getUserRepository().getUserList().stream()
                .filter(u -> u.getUsername().equals(currentUsername))
                .findFirst()
                .orElse(null);

        if (currentUser == null) {
            NotificationManager.showError("Ошибка", "Пользователь не найден");
            return;
        }

        OperatingHoursLogDto logDto = new OperatingHoursLogDto();
        logDto.setHoursAdded(hoursToAdd);
        logDto.setEquipment(selectedItem);
        logDto.setUser(currentUser);

        equipmentRepository.addOperatingHours(logDto)
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Успех", "Наработка успешно добавлена");
                    WindowManager.close(event);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        WindowManager.close(event);
    }
}