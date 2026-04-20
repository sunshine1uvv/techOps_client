package org.example.tech_ops_gui.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.control.textfield.CustomTextField;
import org.example.tech_ops_gui.controllers.bundles.AddInBundleViewController;
import org.example.tech_ops_gui.controllers.bundles.BundleViewController;
import org.example.tech_ops_gui.controllers.bundles.RemoveFromBundleViewController;
import org.example.tech_ops_gui.controllers.crud.DeleteViewController;
import org.example.tech_ops_gui.controllers.crud.EditViewController;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.SessionManager;
import org.example.tech_ops_gui.utils.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class EquipmentViewController {

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
    private TableColumn<EquipmentDto, String> locationCol;
    @FXML
    private TableColumn<EquipmentDto, String> employeeCol;
    @FXML
    private TableColumn<EquipmentDto, Integer> categoryCol;
    @FXML
    private TableColumn<EquipmentDto, String> fullCodeCol;
    @FXML
    private TableColumn<EquipmentDto, String> isBundledCol;
    @FXML
    private CustomTextField searchField;
    @FXML
    private Button addEquipmentBtn;

    private FilteredList<EquipmentDto> filteredData;

    private final EquipmentRepository repository = EquipmentRepository.getInstance();


    @FXML
    private void initialize() {
        configureTableColumns();
        addEquipmentBtn.setVisible("ADMIN".equals(SessionManager.getInstance().getRole()) || "SUPERADMIN".equals(SessionManager.getInstance().getRole()));
        ObservableList<EquipmentDto> source = repository.getEquipmentList();
        filteredData = new FilteredList<>(source, p -> true);
        SortedList<EquipmentDto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(equipmentTable.comparatorProperty());
        equipmentTable.setItems(sortedData);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilter(newVal));
    }


    private void updateFilter(String filter) {
        filteredData.setPredicate(equipment -> {
            if (filter == null || filter.isBlank()) return true;
            String f = filter.toLowerCase();
            return (equipment.getName() != null && equipment.getName().toLowerCase().contains(f)) ||
                    (equipment.getSerialNumber() != null && equipment.getSerialNumber().toLowerCase().contains(f)) ||
                    (equipment.getInventoryNumber() != null && equipment.getInventoryNumber().toLowerCase().contains(f)) ||
                    (equipment.getType() != null && equipment.getType().getName().toLowerCase().contains(f));
        });
    }


    private void configureTableColumns() {
        invNumCol.setCellValueFactory(cellData -> cellData.getValue().getInventoryNumberProperty());
        serialNumCol.setCellValueFactory(cellData -> cellData.getValue().getSerialNumberProperty());
        typeCol.setCellValueFactory(cellData -> cellData.getValue().getType().getNameProperty());
        nameCol.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        locationCol.setCellValueFactory(cellData -> cellData.getValue().getLocationProperty());
        employeeCol.setCellValueFactory(cellData -> {
            UserDto employee = cellData.getValue().getEmployee();
            return employee == null ? new SimpleStringProperty("") : employee.getSurnameProperty();
        });
        categoryCol.setCellValueFactory(cellData -> cellData.getValue().getCategoryProperty());
        fullCodeCol.setCellValueFactory(cellData -> cellData.getValue().getType().getFullCodeProperty());
        isBundledCol.setCellValueFactory(cellData -> {
            boolean hasParent = cellData.getValue().getParent() != null;
            return new ReadOnlyStringWrapper(hasParent ? "Да" : "Нет");
        });
        setupContextMenu();
    }


    private void setupContextMenu() {
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentDto> row = new TableRow<>();
            Runnable buildContextMenu = () -> {
                EquipmentDto item = row.getItem();
                String role = SessionManager.getInstance().getRole();
                boolean isAdmin = "ADMIN".equals(role) || "SUPERADMIN".equals(role);
                boolean isUser = "USER".equals(role);
                List<MenuItem> menuItems = new ArrayList<>();
                if (item != null) {
                    MenuItem viewBundleItem = new MenuItem("Посмотреть комплект");
                    viewBundleItem.setOnAction(e -> loadEquipmentBundleView(item));
                    menuItems.add(viewBundleItem);
                }
                if (isAdmin && item != null) {
                    menuItems.add(new SeparatorMenuItem());
                    MenuItem editItem = new MenuItem("Редактировать");
                    editItem.setOnAction(e -> loadEquipmentEditView(item));
                    menuItems.add(editItem);
                }
                if (!isUser && item != null) {
                    menuItems.add(new SeparatorMenuItem());
                    if (item.getParent() == null) {
                        MenuItem addBundleItem = new MenuItem("Ввести в комплект");
                        addBundleItem.setOnAction(e -> loadEquipmentAddInBundleView(item));
                        menuItems.add(addBundleItem);
                    } else {
                        MenuItem removeBundleItem = new MenuItem("Вывести из комплекта");
                        removeBundleItem.setOnAction(e -> loadEquipmentRemoveFromBundleView(item));
                        menuItems.add(removeBundleItem);
                    }
                }
                if (isAdmin && item != null) {
                    menuItems.add(new SeparatorMenuItem());
                    MenuItem deleteItem = new MenuItem("Удалить");
                    deleteItem.setOnAction(e -> loadEquipmentDeleteView(item));
                    menuItems.add(deleteItem);
                }
                ContextMenu rowMenu = new ContextMenu();
                rowMenu.getItems().setAll(menuItems);
                row.setContextMenu(rowMenu);
            };
            row.itemProperty().addListener((obs, oldItem, newItem) -> buildContextMenu.run());
            row.setContextMenu(null);
            return row;
        });
    }

    @FXML
    private void handleRefreshClick() {
        repository.refresh();
    }

    @FXML
    private void handleAddClick() {
        loadEquipmentAddView();
    }

    private void loadEquipmentEditView(EquipmentDto selectedItem) {
        WindowManager.openModalWindow(
                "crud/equipment-edit-view.fxml",
                "Редактирование",
                param -> new EditViewController(selectedItem)
        );
    }

    private void loadEquipmentDeleteView(EquipmentDto selectedItem) {
        WindowManager.openModalWindow(
                "crud/equipment-delete-view.fxml",
                "Удаление",
                param -> new DeleteViewController(selectedItem)
        );
    }

    private void loadEquipmentAddView() {
        WindowManager.openModalWindow("crud/equipment-add-view.fxml", "Добавление оборудования");
    }

    private void loadEquipmentBundleView(EquipmentDto selectedItem) {
        WindowManager.openModalWindow(
                "bundles/equipment-bundle-view.fxml",
                "Просмотр комплекта",
                param -> new BundleViewController(selectedItem)
        );
    }

    private void loadEquipmentAddInBundleView(EquipmentDto selectedItem) {
        WindowManager.openModalWindow(
                "bundles/equipment-add-in-bundle-view.fxml",
                "Ввод в комплект",
                param -> new AddInBundleViewController(selectedItem)
        );
    }

    private void loadEquipmentRemoveFromBundleView(EquipmentDto selectedItem) {
        WindowManager.openModalWindow(
                "bundles/equipment-remove-from-bundle-view.fxml",
                "Вывод из комплекта",
                param -> new RemoveFromBundleViewController(selectedItem)
        );
    }
}