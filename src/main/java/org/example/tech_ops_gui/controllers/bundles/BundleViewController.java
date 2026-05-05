package org.example.tech_ops_gui.controllers.bundles;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.Cleanable;
import org.example.tech_ops_gui.utils.EquipmentHierarchyUtil;

public class BundleViewController implements Cleanable {

    @FXML private TreeTableView<EquipmentDto> bundleTreeTable;
    @FXML private TreeTableColumn<EquipmentDto, String> nameCol, invNumCol, typeCol, serNumCol;

    private final EquipmentDto selectedItem;
    private final EquipmentRepository equipmentRepository = AppContext.getEquipmentRepository();
    private FilteredList<EquipmentDto> childrenOfRoot;

    private final ListChangeListener<EquipmentDto> dataChangeListener = this::handleDataChange;

    public BundleViewController(EquipmentDto selectedItem) { this.selectedItem = selectedItem; }

    @FXML
    private void initialize() {
        configureColumns();
        configureRows();
        Long rootId = getRootId();

        childrenOfRoot = new FilteredList<>(equipmentRepository.getEquipmentList());
        childrenOfRoot.setPredicate(e -> e.getParent() != null && e.getParent().getId().equals(rootId));

        equipmentRepository.getEquipmentList().addListener(dataChangeListener);
        rebuildTree();
        Platform.runLater(this::setupWindowCloseListener);
    }

    private Long getRootId() { return (selectedItem.getParent() != null) ? selectedItem.getParent().getId() : selectedItem.getId(); }

    private void handleDataChange(ListChangeListener.Change<? extends EquipmentDto> change) {
        Long rootId = getRootId();
        boolean relevant = false;

        while (change.next()) {
            if (change.wasAdded()) relevant = change.getAddedSubList().stream().anyMatch(e -> EquipmentHierarchyUtil.isRelevantForBundle(e, rootId));
            if (change.wasRemoved()) relevant = change.getRemoved().stream().anyMatch(e -> EquipmentHierarchyUtil.isRelevantForBundle(e, rootId));
            if (change.wasUpdated() || change.wasReplaced()) {
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    if (EquipmentHierarchyUtil.isRelevantForBundle(change.getList().get(i), rootId)) { relevant = true; break; }
                }
            }
            if (relevant) break;
        }
        if (relevant) Platform.runLater(this::rebuildTree);
    }

    private void rebuildTree() {
        Long rootId = getRootId();
        EquipmentDto rootEquipment = equipmentRepository.getEquipmentList().stream().filter(e -> e.getId().equals(rootId)).findFirst().orElse(null);

        if (rootEquipment == null) { bundleTreeTable.setRoot(null); return; }

        TreeItem<EquipmentDto> rootNode = new TreeItem<>(rootEquipment);
        rootNode.setExpanded(true);
        childrenOfRoot.forEach(e -> rootNode.getChildren().add(new TreeItem<>(e)));
        bundleTreeTable.setRoot(rootNode);
    }

    private void configureColumns() {
        nameCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getNameProperty());
        invNumCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getInventoryNumberProperty());
        serNumCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getSerialNumberProperty());
        typeCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getType().getNameProperty());
    }

    private void configureRows() {
        bundleTreeTable.setRowFactory(tv -> new javafx.scene.control.TreeTableRow<>() {
            @Override
            protected void updateItem(EquipmentDto item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("root-equipment-row");
                if (!empty && item != null && getTreeItem() != null && getTreeItem().getParent() == null) {
                    getStyleClass().add("root-equipment-row");
                }
            }
        });
    }

    private void setupWindowCloseListener() {
        if (bundleTreeTable.getScene() != null && bundleTreeTable.getScene().getWindow() != null) {
            Stage stage = (Stage) bundleTreeTable.getScene().getWindow();
            stage.setOnHidden(event -> equipmentRepository.getEquipmentList().removeListener(dataChangeListener));
        }
    }

    @Override
    public void cleanup() {
        equipmentRepository.getEquipmentList().removeListener(dataChangeListener);
        bundleTreeTable.setRoot(null);
    }
}