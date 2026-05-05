package org.example.tech_ops_gui.controllers.bundles;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.CustomTextField;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.EquipmentHierarchyUtil;
import org.example.tech_ops_gui.utils.NotificationManager;

public class AttachToBundleViewController {

    @FXML
    private TableView<EquipmentDto> equipmentTable;
    @FXML
    private TableColumn<EquipmentDto, String> invNumCol;
    @FXML
    private TableColumn<EquipmentDto, String> serialNumCol;
    @FXML
    private TableColumn<EquipmentDto, String> typeCol;
    @FXML
    private TableColumn<EquipmentDto, String> nameCol;
    @FXML
    private CustomTextField searchField;

    private final EquipmentRepository equipmentRepository = AppContext.getEquipmentRepository();
    private FilteredList<EquipmentDto> filteredData;
    private final EquipmentDto selectedItem;

    public AttachToBundleViewController(EquipmentDto selectedItem) {
        this.selectedItem = selectedItem;
    }

    @FXML
    private void initialize() {
        configureTableColumns();
        filteredData = new FilteredList<>(equipmentRepository.getEquipmentList(), this::isRootEquipment);
        SortedList<EquipmentDto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(equipmentTable.comparatorProperty());
        equipmentTable.setItems(sortedData);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(EquipmentHierarchyUtil.buildAddInBundlePredicate(selectedItem, newVal))
        );
    }

    private void configureTableColumns() {
        invNumCol.setCellValueFactory(cellData -> cellData.getValue().getInventoryNumberProperty());
        serialNumCol.setCellValueFactory(cellData -> cellData.getValue().getSerialNumberProperty());
        typeCol.setCellValueFactory(cellData -> cellData.getValue().getType() != null ?
                cellData.getValue().getType().getNameProperty() : new ReadOnlyStringWrapper(""));
        nameCol.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentDto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleSelection(row.getItem());
                }
            });
            return row;

        });
    }

    private void handleSelection(EquipmentDto parent) {
        equipmentRepository.attach(parent.getId(), selectedItem.getId())
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Успех", "Оборудование успешно добавлено в комплект.");
                    Stage stage = (Stage) equipmentTable.getScene().getWindow();
                    stage.close();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    private boolean isRootEquipment(EquipmentDto equipment) {
        return equipment.getParent() == null && equipment.getInventoryNumber() != null && equipment != selectedItem;
    }

    private void updateFilter(String filter) {
        filteredData.setPredicate(equipment -> {
            if (!isRootEquipment(equipment)) return false;
            if (filter == null || filter.isBlank()) return true;
            String f = filter.toLowerCase();
            return (equipment.getName() != null && equipment.getName().toLowerCase().contains(f)) ||
                    (equipment.getSerialNumber() != null && equipment.getSerialNumber().toLowerCase().contains(f)) ||
                    (equipment.getInventoryNumber() != null && equipment.getInventoryNumber().toLowerCase().contains(f)) ||
                    (equipment.getType() != null && equipment.getType().getName().toLowerCase().contains(f));
        });
    }
}
