package org.example.tech_ops_gui.controllers.crud;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.services.EquipmentBatchService;
import org.example.tech_ops_gui.utils.NotificationManager;

import java.util.List;

public class ConflictResolverController {
    @FXML private TableView<ConflictWrapper> conflictTable;
    @FXML private TableColumn<ConflictWrapper, Boolean> skipCol;
    @FXML private TableColumn<ConflictWrapper, String> invCol;
    @FXML private TableColumn<ConflictWrapper, String> serialCol;
    @FXML private TableColumn<ConflictWrapper, String> nameCol;
    @FXML private TableColumn<ConflictWrapper, String> reasonCol;

    private final EquipmentBatchService batchService = AppContext.getEquipmentBatchService();
    private boolean confirmed = false;
    private List<EquipmentDto> resultList;

    @FXML
    public void initialize() {
        setupColumns();
        setupRowFactory();
        conflictTable.setEditable(true);
    }

    private void setupColumns() {
        skipCol.setCellValueFactory(cd -> cd.getValue().skipProperty());
        skipCol.setCellFactory(CheckBoxTableCell.forTableColumn(skipCol));

        invCol.setCellValueFactory(cd -> cd.getValue().inventoryNumberProperty());
        invCol.setCellFactory(TextFieldTableCell.forTableColumn());
        invCol.setOnEditCommit(event -> {
            event.getRowValue().setInventoryNumber((event.getNewValue() != null) ? event.getNewValue().trim().toUpperCase() : "");
            revalidateRow(event.getRowValue());
            conflictTable.refresh();
        });

        serialCol.setCellValueFactory(cd -> cd.getValue().serialNumberProperty());
        serialCol.setCellFactory(TextFieldTableCell.forTableColumn());
        serialCol.setOnEditCommit(event -> {
            event.getRowValue().setSerialNumber(event.getNewValue());
            revalidateRow(event.getRowValue());
            conflictTable.refresh();
        });

        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDto().getName()));
        reasonCol.setCellValueFactory(cd -> cd.getValue().reasonProperty());
    }

    private void setupRowFactory() {
        conflictTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ConflictWrapper item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (isSelected()) setStyle("-fx-background-color: #0078d7; -fx-text-background-color: white; -fx-font-weight: bold;");
                else if (!item.isSkipped() && item.hasConflict()) setStyle("-fx-background-color: #ffcccc;");
                else if (item.getReason().equals("Исправлено")) setStyle("-fx-background-color: #e6ffed;");
                else setStyle("");
            }
        });
    }

    private void revalidateRow(ConflictWrapper row) {
        if (row.isSkipped()) { row.setReason(""); return; }

        // Получаем остальные активные (не пропущенные) элементы из таблицы для проверки дубликатов внутри самой таблицы
        List<EquipmentDto> activeSiblings = conflictTable.getItems().stream()
                .filter(w -> w != row && !w.isSkipped())
                .map(w -> {
                    EquipmentDto temp = new EquipmentDto();
                    temp.setInventoryNumber(w.getInventoryNumber());
                    temp.setSerialNumber(w.getSerialNumber());
                    return temp;
                }).toList();

        // Бизнес-логика проверки ушла в сервис
        String reason = batchService.checkConflict(row.getInventoryNumber(), row.getSerialNumber(), activeSiblings);
        row.setReason(reason);
    }

    public void setData(List<EquipmentDto> conflicts) {
        List<ConflictWrapper> wrappers = conflicts.stream().map(dto -> new ConflictWrapper(dto, "")).toList();
        conflictTable.setItems(FXCollections.observableArrayList(wrappers));
        conflictTable.getItems().forEach(this::revalidateRow);
    }

    @FXML
    private void handleAutoFix() {
        List<ConflictWrapper> toFix = conflictTable.getItems().stream().filter(w -> !w.isSkipped() && w.getReason().contains("Инв. номер")).toList();
        if (toFix.isEmpty()) return;

        AppContext.getEquipmentRepository().getNextAvailableInventoryNumbers(toFix.size())
                .thenAccept(newNumbers -> Platform.runLater(() -> {
                    for (int i = 0; i < toFix.size(); i++) {
                        toFix.get(i).setInventoryNumber(newNumbers.get(i));
                        revalidateRow(toFix.get(i));
                    }
                    conflictTable.refresh();
                    NotificationManager.showInfo("Автоисправление", "Номера успешно обновлены.");
                }));
    }

    @FXML
    private void handleConfirm() {
        if (conflictTable.getItems().stream().anyMatch(w -> !w.isSkipped() && w.hasConflict())) {
            NotificationManager.showError("Ошибка", "Исправьте или пропустите все конфликтующие записи.");
            return;
        }

        resultList = conflictTable.getItems().stream()
                .filter(w -> !w.isSkipped())
                .peek(w -> { w.getDto().setInventoryNumber(w.getInventoryNumber()); w.getDto().setSerialNumber(w.getSerialNumber()); })
                .map(ConflictWrapper::getDto).toList();
        confirmed = true;
        ((Stage) conflictTable.getScene().getWindow()).close();
    }

    @FXML private void handleSelectAllSkip() {
        boolean anyUnskipped = conflictTable.getItems().stream().anyMatch(w -> !w.isSkipped());
        conflictTable.getItems().forEach(w -> w.setSkip(anyUnskipped));
        conflictTable.refresh();
    }

    @FXML private void handleCancel() { confirmed = false; ((Stage) conflictTable.getScene().getWindow()).close(); }
    public boolean isConfirmed() { return confirmed; }
    public List<EquipmentDto> getResultList() { return resultList; }

    public static class ConflictWrapper {
        private final EquipmentDto dto;
        private final StringProperty inventoryNumber, serialNumber, reason;
        private final BooleanProperty skip = new SimpleBooleanProperty(false);

        public ConflictWrapper(EquipmentDto dto, String reasonText) {
            this.dto = dto;
            this.inventoryNumber = new SimpleStringProperty(dto.getInventoryNumber());
            this.serialNumber = new SimpleStringProperty(dto.getSerialNumber());
            this.reason = new SimpleStringProperty(reasonText);
        }

        public boolean hasConflict() { String r = reason.get(); return r != null && !r.isEmpty() && !r.equals("Исправлено"); }
        public EquipmentDto getDto() { return dto; }
        public String getInventoryNumber() { return inventoryNumber.get(); }
        public StringProperty inventoryNumberProperty() { return inventoryNumber; }
        public void setInventoryNumber(String v) { inventoryNumber.set(v); }
        public String getSerialNumber() { return serialNumber.get(); }
        public StringProperty serialNumberProperty() { return serialNumber; }
        public void setSerialNumber(String v) { serialNumber.set(v); }
        public String getReason() { return reason.get(); }
        public StringProperty reasonProperty() { return reason; }
        public void setReason(String v) { reason.set(v); }
        public boolean isSkipped() { return skip.get(); }
        public BooleanProperty skipProperty() { return skip; }
        public void setSkip(boolean v) { skip.set(v); }
    }
}