package org.example.tech_ops_gui.controllers.crud;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.OperatingHoursLogDto;
import org.example.tech_ops_gui.enums.UserRole;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.NotificationManager;

import java.time.format.DateTimeFormatter;

public class HoursHistoryViewController {

    // Новые элементы интерфейса
    @FXML
    private Label typeLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label invLabel;

    @FXML
    private TableView<OperatingHoursLogDto> historyTable;
    @FXML
    private TableColumn<OperatingHoursLogDto, String> dateCol;
    @FXML
    private TableColumn<OperatingHoursLogDto, String> userCol;
    @FXML
    private TableColumn<OperatingHoursLogDto, Integer> hoursCol;

    private final EquipmentRepository equipmentRepository = AppContext.getEquipmentRepository();
    private final EquipmentDto selectedItem;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public HoursHistoryViewController(EquipmentDto equipment) {
        this.selectedItem = equipment;
    }

    @FXML
    public void initialize() {
        String type = (selectedItem.getType() != null && selectedItem.getType().getName() != null && !selectedItem.getType().getName().trim().isEmpty())
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
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getLogDate() != null ? data.getValue().getLogDate().format(formatter) : ""
        ));
        userCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUser() != null ?
                        data.getValue().getUser().getSurname() + " " + data.getValue().getUser().getName() + " " + data.getValue().getUser().getPatronymic() : "Неизвестен"
        ));

        hoursCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getHoursAdded()));
        setupContextMenu();
        loadData();
    }

    private void loadData() {
        equipmentRepository.getHoursHistory(selectedItem.getId())
                .thenAccept(logs -> Platform.runLater(() -> historyTable.getItems().setAll(logs)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    private void setupContextMenu() {
        historyTable.setRowFactory(tv -> {
            TableRow<OperatingHoursLogDto> row = new TableRow<>();

            ContextMenu rowMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Отменить (удалить) запись");

            deleteItem.setOnAction(event -> {
                OperatingHoursLogDto log = row.getItem();
                if (log != null) {
                    equipmentRepository.deleteOperatingHours(log.getId())
                            .thenRun(() -> Platform.runLater(() -> {
                                historyTable.getItems().remove(log);
                                NotificationManager.showInfo("Успех", "Запись отменена. Наработка пересчитана.");
                            }))
                            .exceptionally(ex -> {
                                Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                                return null;
                            });
                }
            });

            rowMenu.getItems().add(deleteItem);

            row.itemProperty().addListener((obs, oldLog, newLog) -> {
                if (newLog == null) {
                    row.setContextMenu(null);
                } else {
                    String currentUsername = AppContext.getSessionManager().getUsername();
                    UserRole role = AppContext.getSessionManager().getRole();

                    boolean isAdmin = role == UserRole.ADMIN || role == UserRole.SUPERADMIN;
                    boolean isLogOwner = newLog.getUser() != null && newLog.getUser().getUsername().equals(currentUsername);
                    boolean isEquipOwner = selectedItem.getEmployee() != null && selectedItem.getEmployee().getUsername().equals(currentUsername);

                    if (isAdmin || isLogOwner || isEquipOwner) {
                        row.setContextMenu(rowMenu);
                    } else {
                        row.setContextMenu(null);
                    }
                }
            });
            return row;
        });
    }
}