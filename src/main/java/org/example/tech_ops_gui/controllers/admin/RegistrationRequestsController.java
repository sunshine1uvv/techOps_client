package org.example.tech_ops_gui.controllers.admin;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.RequestResponseDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.services.AdminService;
import org.example.tech_ops_gui.synchronization.RequestResponseSyncMessage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;
import org.example.tech_ops_gui.utils.Cleanable;
import org.example.tech_ops_gui.utils.NotificationManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RegistrationRequestsController implements Cleanable {

    @FXML private ComboBox<String> filterComboBox;
    @FXML private TableView<RequestResponseDto> requestsTable;
    @FXML private TableColumn<RequestResponseDto, String> colUsername;
    @FXML private TableColumn<RequestResponseDto, String> colName;
    @FXML private TableColumn<RequestResponseDto, String> colSurname;
    @FXML private TableColumn<RequestResponseDto, String> colRank;
    @FXML private TableColumn<RequestResponseDto, String> colPhone;
    @FXML private TableColumn<RequestResponseDto, String> colStatus;

    private final AdminService adminService = AppContext.getAdminService();
    private final ObservableList<RequestResponseDto> registrationRequestList = FXCollections.observableArrayList();
    private final WebSocketSyncClient syncService = AppContext.getWebSocketClient();
    private final Consumer<RequestResponseSyncMessage> requestResponseHandler = this::handleRequestResponseSyncMessage;

    @FXML
    public void initialize() {
        syncService.connect();
        syncService.subscribeRequests(requestResponseHandler);
        filterComboBox.setItems(FXCollections.observableArrayList("Активные", "Рассмотренные"));
        filterComboBox.getSelectionModel().select("Активные");
        filterComboBox.setOnAction(e -> refreshRequests());
        configureTableColumns();
        refreshRequests();
    }

    private void configureTableColumns() {
        requestsTable.setRowFactory(tv -> new TableRow<RequestResponseDto>() {
            @Override
            protected void updateItem(RequestResponseDto item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("approved-row", "rejected-row", "pending-row");
                if (item == null || empty) {
                    return;
                }
                switch (item.getStatus()) {
                    case "APPROVED":
                        getStyleClass().add("approved-row");
                        break;
                    case "REJECTED":
                        getStyleClass().add("rejected-row");
                        break;
                    case "PENDING":
                        getStyleClass().add("pending-row");
                        break;
                }
            }
        });
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        colRank.setCellValueFactory(new PropertyValueFactory<>("militaryRank"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colStatus.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus();
            String rusStatus = switch (status) {
                case "PENDING" -> "Ожидает подтверждения";
                case "APPROVED" -> "Одобрено";
                case "REJECTED" -> "Отклонено";
                default -> status;
            };
            return new SimpleStringProperty(rusStatus);
        });
        requestsTable.setItems(registrationRequestList);
    }


    @FXML
    public void refreshRequests() {
        String selectedFilter = filterComboBox.getSelectionModel().getSelectedItem();
        CompletableFuture<List<RequestResponseDto>> future;

        if ("Активные".equals(selectedFilter)) {
            future = CompletableFuture.supplyAsync(() -> adminService.getRequestsByStatus("PENDING"));
        } else {
            future = CompletableFuture.supplyAsync(() -> adminService.getReviewedRequests());
        }
        future.thenAccept(requests -> Platform.runLater(() -> {
                    registrationRequestList.setAll(requests);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    @FXML
    public void approveRequest() {
        RequestResponseDto selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationManager.showWarning("Ошибка", "Выберите заявку");
            return;
        }
        CompletableFuture.runAsync(() -> adminService.reviewRequest(selected.getId(), "APPROVED",null))
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showWarning("Успешно", "Заявка одобрена");
                    refreshRequests();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    @FXML
    public void rejectRequest() {
        RequestResponseDto selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationManager.showError("Ошибка", "Выберите заявку");
            return;
        }
        CompletableFuture.runAsync(() -> adminService.reviewRequest(selected.getId(), "REJECTED", null))
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Успешно", "Заявка отклонена");
                    refreshRequests();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    @FXML
    public void restoreRequest() {
        RequestResponseDto selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationManager.showInfo("Ошибка", "Выберите заявку");
            return;
        }
        if (!"REJECTED".equals(selected.getStatus())) {
            NotificationManager.showInfo("Ошибка", "Восстановить можно только отклонённую заявку");
            return;
        }
        CompletableFuture.runAsync(() -> adminService.restoreRequest(selected.getId()))
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Успешно", "Заявка восстановлена");
                    refreshRequests();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    private boolean matchesCurrentFilter(RequestResponseDto request) {
        String selectedFilter = filterComboBox.getSelectionModel().getSelectedItem();
        if (selectedFilter == null) return true;
        if ("Активные".equals(selectedFilter)) {
            return "PENDING".equals(request.getStatus());
        } else {
            return "APPROVED".equals(request.getStatus()) || "REJECTED".equals(request.getStatus());
        }
    }

    public void handleRequestResponseSyncMessage(RequestResponseSyncMessage message) {
        Platform.runLater(() -> {
            String action = message.getAction();
            List<RequestResponseDto> items = message.getPayload();
            if (items == null) return;
            for (RequestResponseDto incomingItem : items) {
                boolean matchesFilter = matchesCurrentFilter(incomingItem);
                switch (action) {
                    case "CREATE" -> {
                        if (matchesFilter && registrationRequestList.stream().noneMatch(e -> e.getId().equals(incomingItem.getId()))) {
                            registrationRequestList.add(incomingItem);
                        }
                    }
                    case "UPDATE" -> {
                        if (matchesFilter) {
                            for (int i = 0; i < registrationRequestList.size(); i++) {
                                if (registrationRequestList.get(i).getId().equals(incomingItem.getId())) {
                                    registrationRequestList.set(i, incomingItem);
                                    break;
                                }
                            }
                        } else {
                            registrationRequestList.removeIf(item -> item.getId().equals(incomingItem.getId()));
                        }
                    }
                    case "DELETE" -> registrationRequestList.removeIf(item -> item.getId().equals(incomingItem.getId()));
                }
            }
        });
    }

    @Override
    public void cleanup() {
        syncService.unsubscribeRequests(requestResponseHandler);
    }
}