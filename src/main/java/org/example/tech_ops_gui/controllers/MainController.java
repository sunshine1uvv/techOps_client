package org.example.tech_ops_gui.controllers;


import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.entities.User;
import org.example.tech_ops_gui.entities.UserStatus;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.UserRepository;
import org.example.tech_ops_gui.utils.Cleanable;
import org.example.tech_ops_gui.utils.SessionManager;

import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button registrationRequestsViewBtn;
    @FXML
    private Button usersViewBtn;
    @FXML
    private BorderPane rootPane;

    private Object currentController;
    private String currentRole;
    private final UserRepository userRepository = UserRepository.getInstance();


    @FXML
    private void initialize() {
        currentRole = SessionManager.getInstance().getRole();
        registrationRequestsViewBtn.setVisible("SUPERADMIN".equals(currentRole));
        usersViewBtn.setVisible("SUPERADMIN".equals(currentRole));
        userRepository.getUserList().addListener((ListChangeListener<UserDto>) change ->
                checkCurrentUserStatus()
        );
        checkCurrentUserStatus();
        loadEquipmentView();
    }

    private void checkCurrentUserStatus() {
        String currentUsername = SessionManager.getInstance().getUsername();
        if (currentUsername == null) return;

        if(userRepository.getUserList().isEmpty()) {
            return;
        }
        UserDto currentUser = userRepository.getUserList().stream()
                .filter(u -> u.getUsername().equals(currentUsername))
                .findFirst()
                .orElse(null);

        if (currentUser == null) {
            showFatalErrorAndExit("Ваша учётная запись была удалена. Приложение будет закрыто.");
            return;
        }

        if (currentUser.getStatus() == UserStatus.BLOCKED) {
            showFatalErrorAndExit("Ваша учётная запись заблокирована. Приложение будет закрыто.");
        }
    }

    private void showFatalErrorAndExit(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка доступа");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            Platform.exit();
        });
    }

    @FXML
    private void loadEquipmentView() {
       loadView("/org/example/tech_ops_gui/fxml/equipment-view.fxml");
    }

    @FXML
    private void loadRegistrationRequestView() {
        if (!"SUPERADMIN".equals(currentRole)) return;
        loadView("/org/example/tech_ops_gui/fxml/admin/registration-requests-view.fxml");
    }

    @FXML
    private void loadUsersView() {
        if (!"SUPERADMIN".equals(currentRole)) return;
        loadView("/org/example/tech_ops_gui/fxml/users/user-management-view.fxml");
    }

    @FXML
    private void loadProfileView() {
        loadView("/org/example/tech_ops_gui/fxml/users/profile-view.fxml");
    }

    @FXML
    private void logout() {
        try {
            SessionManager.getInstance().clear();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/tech_ops_gui/fxml/auth/login-view.fxml"));
            stage.setTitle("Авторизация");
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.show();
        } catch (Exception e) {
            CustomExceptionHandler.handleError(e);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            if (currentController instanceof Cleanable) {
                ((Cleanable) currentController).cleanup();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            currentController = loader.getController();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            CustomExceptionHandler.handleError(e);
        }
    }
}
