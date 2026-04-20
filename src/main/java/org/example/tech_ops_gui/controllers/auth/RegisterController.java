package org.example.tech_ops_gui.controllers.auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.tech_ops_gui.dto.RegistrationRequestDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.services.AuthService;
import org.example.tech_ops_gui.utils.NotificationManager;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField patronymicField;
    @FXML private TextField militaryRankField;
    @FXML private TextField phoneField;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleSubmit() {
        RegistrationRequestDto dto = new RegistrationRequestDto();
        dto.setUsername(usernameField.getText().trim());
        dto.setPassword(passwordField.getText());
        dto.setName(nameField.getText().trim());
        dto.setSurname(surnameField.getText().trim());
        dto.setPatronymic(patronymicField.getText().trim());
        dto.setMilitaryRank(militaryRankField.getText().trim());
        dto.setPhoneNumber(phoneField.getText().trim());

        try {
            authService.register(dto);
            NotificationManager.showInfo("Заявка отправлена", "Ваша заявка на регистрацию отправлена. Ожидайте одобрения.");
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/tech_ops_gui/fxml/auth/login-view.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Авторизация");
        } catch (Exception e) {
            CustomExceptionHandler.handleError(e);
        }
    }

    @FXML
    private void handleBack() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/tech_ops_gui/fxml/auth/login-view.fxml"));
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Авторизация");
    }
}
