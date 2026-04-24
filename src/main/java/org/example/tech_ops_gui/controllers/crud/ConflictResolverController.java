package org.example.tech_ops_gui.controllers.crud;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.NotificationManager;

import java.util.*;
import java.util.stream.Collectors;

public class ConflictResolverController {
    @FXML private TableView<ConflictWrapper> conflictTable;
    @FXML private TableColumn<ConflictWrapper, Boolean> skipCol;
    @FXML private TableColumn<ConflictWrapper, String> invCol;
    @FXML private TableColumn<ConflictWrapper, String> serialCol;
    @FXML private TableColumn<ConflictWrapper, String> nameCol;
    @FXML private TableColumn<ConflictWrapper, String> reasonCol;

    private final EquipmentRepository repository = EquipmentRepository.getInstance();
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

        // --- МГНОВЕННАЯ ПРОВЕРКА ПРИ НАЖАТИИ ENTER ---
        invCol.setOnEditCommit(event -> {
            ConflictWrapper rowData = event.getRowValue();
            String newValue = (event.getNewValue() != null) ? event.getNewValue().trim().toUpperCase() : "";

            // 1. Обновляем значение в модели
            rowData.setInventoryNumber(newValue);

            // 2. Запускаем перепроверку конкретно этой строки
            revalidateRow(rowData);

            // 3. Обновляем таблицу, чтобы перерисовались цвета
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
                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Базовый приоритет стилей:
                    // 1. Если строка выбрана (курсором) - делаем её заметной (синий контур или фон)
                    // 2. Если есть конфликт и не пропущено - красный фон
                    // 3. Если исправлено - стандартный фон

                    if (isSelected()) {
                        setStyle("-fx-background-color: #0078d7; -fx-text-background-color: white; -fx-font-weight: bold;");
                    } else if (!item.isSkipped() && item.hasConflict()) {
                        setStyle("-fx-background-color: #ffcccc;"); // Бледно-красный для ошибок
                    } else if (item.getReason().equals("Исправлено")) {
                        setStyle("-fx-background-color: #e6ffed;"); // Бледно-зеленый для исправленных
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    /**
     * Проверяет одну конкретную строку на конфликты с базой и другими строками
     */
    private void revalidateRow(ConflictWrapper row) {
        if (row.isSkipped()) {
            row.setReason("");
            return;
        }

        String inv = row.getInventoryNumber();
        String ser = row.getSerialNumber();

        Set<String> dbInvs = repository.findAllInventoryNumbers();
        Set<String> dbSerials = repository.findAllSerialNumbers();

        // Проверка на дубликаты внутри текущей таблицы (среди непропущенных)
        boolean internalInvDup = conflictTable.getItems().stream()
                .filter(w -> w != row && !w.isSkipped())
                .anyMatch(w -> Objects.equals(w.getInventoryNumber(), inv));

        boolean internalSerDup = conflictTable.getItems().stream()
                .filter(w -> w != row && !w.isSkipped())
                .anyMatch(w -> Objects.equals(w.getSerialNumber(), ser));

        StringBuilder sb = new StringBuilder();
        if (inv != null && (dbInvs.contains(inv) || internalInvDup)) sb.append("Инв. номер занят; ");
        if (ser != null && (dbSerials.contains(ser) || internalSerDup)) sb.append("Сер. номер занят; ");

        if (sb.length() == 0) {
            row.setReason("Исправлено");
        } else {
            row.setReason(sb.toString().replaceAll("; $", ""));
        }
    }

    public void setData(List<EquipmentDto> conflicts) {
        List<ConflictWrapper> wrappers = conflicts.stream()
                .map(dto -> new ConflictWrapper(dto, ""))
                .collect(Collectors.toList());

        conflictTable.setItems(FXCollections.observableArrayList(wrappers));
        // Первичная валидация всех строк
        conflictTable.getItems().forEach(this::revalidateRow);
    }

    @FXML
    private void handleAutoFix() {
        List<ConflictWrapper> toFix = conflictTable.getItems().stream()
                .filter(w -> !w.isSkipped() && w.getReason().contains("Инв. номер"))
                .toList();

        if (toFix.isEmpty()) return;

        repository.getNextAvailableInventoryNumbers(toFix.size())
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
        // Финальная проверка перед выходом
        boolean hasErrors = conflictTable.getItems().stream()
                .anyMatch(w -> !w.isSkipped() && w.hasConflict());

        if (hasErrors) {
            NotificationManager.showError("Ошибка", "Исправьте или пропустите все конфликтующие записи.");
            return;
        }

        resultList = conflictTable.getItems().stream()
                .filter(w -> !w.isSkipped())
                .peek(w -> {
                    w.getDto().setInventoryNumber(w.getInventoryNumber());
                    w.getDto().setSerialNumber(w.getSerialNumber());
                })
                .map(ConflictWrapper::getDto)
                .collect(Collectors.toList());
        confirmed = true;
        closeStage();
    }

    @FXML private void handleSelectAllSkip() {
        boolean anyUnskipped = conflictTable.getItems().stream().anyMatch(w -> !w.isSkipped());
        conflictTable.getItems().forEach(w -> w.setSkip(anyUnskipped));
        conflictTable.refresh();
    }

    @FXML private void handleCancel() { confirmed = false; closeStage(); }
    private void closeStage() { ((Stage) conflictTable.getScene().getWindow()).close(); }
    public boolean isConfirmed() { return confirmed; }
    public List<EquipmentDto> getResultList() { return resultList; }

    // --- ОБНОВЛЕННАЯ ОБЕРТКА ---
    public static class ConflictWrapper {
        private final EquipmentDto dto;
        private final StringProperty inventoryNumber;
        private final StringProperty serialNumber;
        private final StringProperty reason;
        private final BooleanProperty skip = new SimpleBooleanProperty(false);

        public ConflictWrapper(EquipmentDto dto, String reasonText) {
            this.dto = dto;
            this.inventoryNumber = new SimpleStringProperty(dto.getInventoryNumber());
            this.serialNumber = new SimpleStringProperty(dto.getSerialNumber());
            this.reason = new SimpleStringProperty(reasonText);
        }

        public boolean hasConflict() {
            String r = reason.get();
            return r != null && !r.isEmpty() && !r.equals("Исправлено");
        }

        public EquipmentDto getDto() { return dto; }
        public String getInventoryNumber() { return inventoryNumber.get(); }
        public StringProperty inventoryNumberProperty() { return inventoryNumber; }
        public void setInventoryNumber(String value) { inventoryNumber.set(value); }

        public String getSerialNumber() { return serialNumber.get(); }
        public StringProperty serialNumberProperty() { return serialNumber; }
        public void setSerialNumber(String value) { serialNumber.set(value); }

        public String getReason() { return reason.get(); }
        public StringProperty reasonProperty() { return reason; }
        public void setReason(String value) { reason.set(value); }

        public boolean isSkipped() { return skip.get(); }
        public BooleanProperty skipProperty() { return skip; }
        public void setSkip(boolean value) { skip.set(value); }
    }
}