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
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.enums.UserRole;
import org.example.tech_ops_gui.enums.UserStatus;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.UserRepository;
import org.example.tech_ops_gui.utils.Cleanable;

public class MainController implements Cleanable {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button registrationRequestsViewBtn;
    @FXML
    private Button usersViewBtn;
    @FXML
    private BorderPane rootPane;
    @FXML
    private Button adminMenuBtn;

    private Object currentController;
    private UserRole currentRole;

    private final UserRepository userRepository = AppContext.getUserRepository();

    private final ListChangeListener<UserDto> userListListener = change -> checkCurrentUserStatus();


    @FXML
    private void initialize() {
        currentRole = AppContext.getSessionManager().getRole();
        adminMenuBtn.managedProperty().bind(adminMenuBtn.visibleProperty());
        registrationRequestsViewBtn.managedProperty().bind(registrationRequestsViewBtn.visibleProperty());
        usersViewBtn.managedProperty().bind(usersViewBtn.visibleProperty());


        boolean isSuperAdmin = (currentRole == UserRole.SUPERADMIN);
        adminMenuBtn.setVisible(isSuperAdmin);
        registrationRequestsViewBtn.setVisible(isSuperAdmin);
        usersViewBtn.setVisible(isSuperAdmin);

        userRepository.getUserList().addListener(userListListener);

        checkCurrentUserStatus();
        loadEquipmentView();
    }

    private void checkCurrentUserStatus() {
        String currentUsername = AppContext.getSessionManager().getUsername();
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
        if (UserRole.SUPERADMIN != currentRole) return;
        loadView("/org/example/tech_ops_gui/fxml/admin/registration-requests-view.fxml");
    }

    @FXML
    private void loadUsersView() {
        if (UserRole.SUPERADMIN != currentRole) return;
        loadView("/org/example/tech_ops_gui/fxml/users/user-management-view.fxml");
    }

    @FXML
    private void loadProfileView() {
        loadView("/org/example/tech_ops_gui/fxml/users/profile-view.fxml");
    }

    @FXML
    private void loadDirectoriesView() {
        if (UserRole.SUPERADMIN != currentRole) return;
        loadView("/org/example/tech_ops_gui/fxml/admin/directories-view.fxml");
    }

    @FXML
    private void logout() {
        try {
            this.cleanup();
            if (currentController instanceof Cleanable) {
                ((Cleanable) currentController).cleanup();
            }

            AppContext.getSessionManager().clear();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup() {
        if (userRepository != null && userListListener != null) {
            userRepository.getUserList().removeListener(userListListener);
        }
    }
}
