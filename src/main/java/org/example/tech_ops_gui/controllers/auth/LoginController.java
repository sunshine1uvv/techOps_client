package org.example.tech_ops_gui.controllers.auth;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.services.AuthService;
import org.example.tech_ops_gui.utils.NotificationManager;


public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final AuthService authService = AppContext.getAuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            NotificationManager.showError("Ошибка", "Введите логин и пароль");
            return;
        }


        authService.login(username, password)
                .thenAccept(resp -> Platform.runLater(() -> {
                    try {
                        AppContext.onUserLogin();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tech_ops_gui/fxml/main-layout.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) usernameField.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Главная - " + resp.getUsername());
                        stage.setMaximized(true);
                    } catch (Exception e) {
                        CustomExceptionHandler.handleError(e);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    @FXML
    private void handleRegister() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/tech_ops_gui/fxml/auth/register-view.fxml"));
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Регистрация");
    }
}
