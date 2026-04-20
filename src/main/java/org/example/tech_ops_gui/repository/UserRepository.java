package org.example.tech_ops_gui.repository;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.services.UserService;
import org.example.tech_ops_gui.synchronization.UserSyncMessage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;

import java.util.List;

public class UserRepository {

    private static UserRepository instance;
    private final ObservableList<UserDto> userList = FXCollections.observableArrayList();
    private final WebSocketSyncClient syncClient = WebSocketSyncClient.getInstance();
    private final UserService service = UserService.getInstance();

    private UserRepository() {
        syncClient.subscribeUsers(this::handleSyncMessage);
    }

    public static UserRepository getInstance() {
        if (instance == null) instance = new UserRepository();
        return instance;
    }

    public void initData() {
        loadUsersFromServer();
    }

    public ObservableList<UserDto> getUserList() {
        return this.userList;
    }

    private void loadUsersFromServer() {
        service.getAllUsers().thenAccept(list ->
                Platform.runLater(() -> userList.setAll(list))
        );
    }

    public void handleSyncMessage(UserSyncMessage message) {
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
        });
    }
}
