package org.example.tech_ops_gui.controllers.bundles;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.repository.EquipmentRepository;

public class BundleViewController {

    @FXML
    private TreeTableView<EquipmentDto> bundleTreeTable;
    @FXML
    private TreeTableColumn<EquipmentDto, String> nameCol;
    @FXML
    private TreeTableColumn<EquipmentDto, String> invNumCol;
    @FXML
    private TreeTableColumn<EquipmentDto, String> typeCol;
    @FXML
    private TreeTableColumn<EquipmentDto, String> serNumCol;

    private final EquipmentDto selectedItem;
    private final EquipmentRepository equipmentRepository = EquipmentRepository.getInstance();

    private FilteredList<EquipmentDto> childrenOfRoot;
    private final ListChangeListener<EquipmentDto> dataChangeListener = this::handleDataChange;

    public BundleViewController(EquipmentDto selectedItem) {
        this.selectedItem = selectedItem;
    }

    @FXML
    private void initialize() {
        configureColumns();
        configureRows();

        Long rootId = getRootId();

        // Фильтрованный список только дочерних элементов корня
        childrenOfRoot = new FilteredList<>(equipmentRepository.getEquipmentList());
        childrenOfRoot.setPredicate(e -> e.getParent() != null && e.getParent().getId().equals(rootId));

        // Слушаем изменения в основном списке
        equipmentRepository.getEquipmentList().addListener(dataChangeListener);

        // Первоначальное построение дерева
        rebuildTree();

        Platform.runLater(this::setupWindowCloseListener);
    }

    private Long getRootId() {
        return (selectedItem.getParent() != null) ? selectedItem.getParent().getId() : selectedItem.getId();
    }

    /**
     * Проверяет, относится ли изменение оборудования к нашему комплекту.
     */
    private boolean isRelevant(EquipmentDto equipment, Long rootId) {
        if (equipment == null) return false;
        // Это сам корень
        if (equipment.getId().equals(rootId)) return true;
        // Это прямой потомок корня
        return equipment.getParent() != null && equipment.getParent().getId().equals(rootId);
    }

    /**
     * Обработчик изменений в общем списке оборудования.
     * Перестраивает дерево только если изменения затрагивают наш комплект.
     */
    private void handleDataChange(ListChangeListener.Change<? extends EquipmentDto> change) {
        Long rootId = getRootId();
        boolean relevant = false;

        while (change.next()) {
            if (change.wasAdded()) {
                relevant = change.getAddedSubList().stream().anyMatch(e -> isRelevant(e, rootId));
            }
            if (change.wasRemoved()) {
                relevant = change.getRemoved().stream().anyMatch(e -> isRelevant(e, rootId));
            }
            if (change.wasUpdated() || change.wasReplaced()) {
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    if (isRelevant(change.getList().get(i), rootId)) {
                        relevant = true;
                        break;
                    }
                }
            }
            if (relevant) break;
        }

        if (relevant) {
            Platform.runLater(this::rebuildTree);
        }
    }

    private void rebuildTree() {
        Long rootId = getRootId();

        // Находим актуальный корневой объект (он мог измениться)
        EquipmentDto rootEquipment = equipmentRepository.getEquipmentList().stream()
                .filter(e -> e.getId().equals(rootId))
                .findFirst()
                .orElse(null);

        if (rootEquipment == null) {
            bundleTreeTable.setRoot(null);
            return;
        }

        TreeItem<EquipmentDto> rootNode = new TreeItem<>(rootEquipment);
        rootNode.setExpanded(true);

        // Заполняем дочерние элементы из отфильтрованного списка
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
        bundleTreeTable.setRowFactory(tv -> new javafx.scene.control.TreeTableRow<EquipmentDto>() {
            @Override
            protected void updateItem(EquipmentDto item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("root-equipment-row");
                if (!empty && item != null) {
                    if (getTreeItem() != null && getTreeItem().getParent() == null) {
                        getStyleClass().add("root-equipment-row");
                    }
                }
            }
        });
    }

    private void setupWindowCloseListener() {
        if (bundleTreeTable.getScene() != null && bundleTreeTable.getScene().getWindow() != null) {
            Stage stage = (Stage) bundleTreeTable.getScene().getWindow();
            stage.setOnHidden(event -> {
                equipmentRepository.getEquipmentList().removeListener(dataChangeListener);
            });
        }
    }
}