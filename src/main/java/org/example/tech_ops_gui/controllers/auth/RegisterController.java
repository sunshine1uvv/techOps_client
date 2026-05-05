package org.example.tech_ops_gui.controllers.auth;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.RegistrationRequestDto;
import org.example.tech_ops_gui.enums.MilitaryRank;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.services.AuthService;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.UserValidator;

import java.util.List;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField patronymicField;
    @FXML private ComboBox<MilitaryRank> militaryRankCombo;
    @FXML private TextField phoneField;

    private final AuthService authService = AppContext.getAuthService();

    @FXML
    private void handleSubmit() {
        RegistrationRequestDto dto = new RegistrationRequestDto();
        dto.setUsername(usernameField.getText().trim());
        dto.setPassword(passwordField.getText());
        dto.setName(nameField.getText().trim());
        dto.setSurname(surnameField.getText().trim());
        dto.setPatronymic(patronymicField.getText().trim());
        MilitaryRank selectedRank = militaryRankCombo.getValue();
        dto.setMilitaryRank(selectedRank != null ? selectedRank.name() : null);
        dto.setPhoneNumber(phoneField.getText().trim());
        List<String> errors = UserValidator.validateRegistration(dto); // Или .validate(dto), если вы переименовали метод
        if (!errors.isEmpty()) {
            NotificationManager.showError("Ошибка заполнения", String.join("\n", errors));
            return;
        }

        authService.register(dto)
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Заявка отправлена", "Ваша заявка на регистрацию отправлена. Ожидайте одобрения администратором.");
                    try {
                        handleBack();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/tech_ops_gui/fxml/auth/login-view.fxml"));
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Авторизация");
    }
}
