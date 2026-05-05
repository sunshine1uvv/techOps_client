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

    private final ObservableList<UserDto> userList = FXCollections.observableArrayList();
    private final UserService service;
    private final WebSocketSyncClient syncClient;

    public UserRepository(UserService service, WebSocketSyncClient syncClient) {
        this.service = service;
        this.syncClient = syncClient;
        this.syncClient.subscribeUsers(this::handleSyncMessage);
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
