package org.example.tech_ops_gui.controllers.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.entities.User;
import org.example.tech_ops_gui.entities.UserRole;
import org.example.tech_ops_gui.entities.UserStatus;
import org.example.tech_ops_gui.services.UserService;
import org.example.tech_ops_gui.synchronization.UserSyncMessage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;
import org.example.tech_ops_gui.utils.Cleanable;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class UserManagementController implements Cleanable {

    @FXML
    private TilePane userCardsContainer;
    private Stage currentStage;

    private final UserService userService = UserService.getInstance();
    private final ObservableList<UserDto> userList = FXCollections.observableArrayList();;
    private final WebSocketSyncClient syncService = WebSocketSyncClient.getInstance();
    private final Consumer<UserSyncMessage> usersHandler = this::handleUserSyncMessage;

    @FXML
    public void initialize() {
        syncService.connect();
        syncService.subscribeUsers(usersHandler);
        loadUsers();
    }

    private void loadUsers() {
        userService.getAllUsers()
                .thenAccept(list -> {
                    userList.setAll(list);
                    displayUsers(userList);
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        System.err.println("Ошибка загрузки пользователей: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private void displayUsers(List<UserDto> users) {
        Platform.runLater(() -> {
            userCardsContainer.getChildren().clear();
            for (UserDto user : users) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tech_ops_gui/fxml/users/user-card.fxml"));
                    Node card = loader.load();
                    UserCardController controller = loader.getController();

                    controller.setUser(user, new UserCardController.UserActionHandler() {
                        @Override
                        public void onToggleStatus(UserDto user, UserStatus newStatus) {
                            userService.updateUserStatus(user.getId(), newStatus);
                        }

                        @Override
                        public void onToggleRole(UserDto user, UserRole newRole) {
                            userService.updateUserRole(user.getId(), newRole);
                        }

                        @Override
                        public void onDelete(UserDto user) {
                            userService.deleteUser(user.getId());
                        }
                    });

                    userCardsContainer.getChildren().add(card);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void refreshUserCards() {
        loadUsers();
    }

    public void handleUserSyncMessage(UserSyncMessage message) {
        Platform.runLater(() -> {
            String action = message.getAction();
            List<UserDto> items = message.getPayload();
            if (items == null) return;

            for (UserDto incomingItem : items) {
                switch (action) {
                    case "CREATE" -> {
                        if (userList.stream().noneMatch(e -> e.getId().equals(incomingItem.getId()))) {
                            userList.add(incomingItem);
                        }
                    }
                    case "UPDATE" -> {
                        for (int i = 0; i < userList.size(); i++) {
                            if (userList.get(i).getId().equals(incomingItem.getId())) {
                                userList.set(i, incomingItem);
                                break;
                            }
                        }
                    }
                    case "DELETE" -> userList.removeIf(item -> item.getId().equals(incomingItem.getId()));
                }
            }
            displayUsers(userList);
        });
    }

    @Override
    public void cleanup() {
        syncService.unsubscribeUsers(usersHandler);
        System.out.println("UsersView отписался от обновлений.");
    }

}