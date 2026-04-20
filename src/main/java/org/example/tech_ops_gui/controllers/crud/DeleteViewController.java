package org.example.tech_ops_gui.controllers.crud;

import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.WindowManager;

public class DeleteViewController {

    @FXML
    private TableColumn<EquipmentDto, String> invNumCol;
    @FXML
    private TableColumn<EquipmentDto, String> typeCol;
    @FXML
    private TableColumn<EquipmentDto, String> nameCol;
    @FXML
    private TableColumn<EquipmentDto, String> serNumCol;
    @FXML
    private TableView<EquipmentDto> childrenTable;
    @FXML
    private VBox warningContainer;

    private final EquipmentDto selectedItem;
    private final EquipmentRepository equipmentRepository = EquipmentRepository.getInstance();
    private FilteredList<EquipmentDto> filteredChildren;

    public DeleteViewController(EquipmentDto selectedItem) {
        this.selectedItem = selectedItem;
    }

    @FXML
    private void initialize() {
        configureTableColumns();
        warningContainer.managedProperty().bind(warningContainer.visibleProperty());

        filteredChildren = new FilteredList<>(equipmentRepository.getEquipmentList());
        filteredChildren.setPredicate(this::isChildOfSelected);

        childrenTable.setItems(filteredChildren);

        filteredChildren.addListener((ListChangeListener<EquipmentDto>) c -> {
            warningContainer.setVisible(!filteredChildren.isEmpty());
        });
    }

    private boolean isChildOfSelected(EquipmentDto equipment) {
        return equipment.getParent() != null &&
                equipment.getParent().getId().equals(selectedItem.getId());
    }


    private void configureTableColumns() {
        invNumCol.setCellValueFactory(data -> data.getValue().getInventoryNumberProperty());
        typeCol.setCellValueFactory(data -> data.getValue().getType().getNameProperty());
        serNumCol.setCellValueFactory(data -> data.getValue().getSerialNumberProperty());
        nameCol.setCellValueFactory(data -> data.getValue().getNameProperty());
    }

    @FXML
    private void handleDeleteClick(ActionEvent event) {
        equipmentRepository.delete(selectedItem.getId());
    }


    @FXML
    private void handleCloseClick(ActionEvent event) {
        WindowManager.close(event);
    }

}
