package org.example.tech_ops_gui.controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.controllers.bundles.AttachToBundleViewController;
import org.example.tech_ops_gui.controllers.bundles.BundleViewController;
import org.example.tech_ops_gui.controllers.bundles.DetachFromBundleViewController;
import org.example.tech_ops_gui.controllers.crud.AddHoursViewController;
import org.example.tech_ops_gui.controllers.crud.DeleteViewController;
import org.example.tech_ops_gui.controllers.crud.EditViewController;
import org.example.tech_ops_gui.controllers.crud.HoursHistoryViewController;
import org.example.tech_ops_gui.dto.DepartmentDto;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.enums.UserRole;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.repository.EquipmentTypeRepository;
import org.example.tech_ops_gui.repository.UserRepository;
import org.example.tech_ops_gui.services.ExcelExportService;
import org.example.tech_ops_gui.utils.FileSelectionUtil;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.SessionManager;
import org.example.tech_ops_gui.utils.WindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EquipmentViewController {

    @FXML
    private Label recordCountLabel;
    @FXML
    private TableView<EquipmentDto> equipmentTable;
    @FXML
    private TableColumn<EquipmentDto, Void> numberCol;
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
    private TableColumn<EquipmentDto, String> departmentCol;
    @FXML
    private TableColumn<EquipmentDto, String> employeeCol;
    @FXML
    private TableColumn<EquipmentDto, Integer> categoryCol;
    @FXML
    private TableColumn<EquipmentDto, String> fullCodeCol;
    @FXML
    private TableColumn<EquipmentDto, String> isBundledCol;
    @FXML
    private TableColumn<EquipmentDto, Integer> currentHoursCol;
    @FXML
    private TableColumn<EquipmentDto, Integer> maxHoursCol;
    @FXML
    private Button addEquipmentBtn;

    //----------------------------------------------------------------------------------

    @FXML
    private AnchorPane slidingPanel;
    @FXML
    private Button toggleMenuBtn;

    @FXML
    private TextField searchInvNum;
    @FXML
    private TextField searchSerial;
    @FXML
    private TextField searchName;
    @FXML
    private SearchableComboBox<EquipmentTypeDto> searchType;
    @FXML
    private TextField searchFullCode;
    @FXML
    private ComboBox<Integer> searchCategory;
    @FXML
    private SearchableComboBox<UserDto> searchEmployee;
    @FXML
    private TextField searchLocation;
    @FXML
    private CheckBox onlyFreeCheck;
    @FXML
    private CheckBox onlyBundledCheck;

    @FXML
    private SearchableComboBox<DepartmentDto> searchDepartment;
    @FXML
    private TextField searchCurrentHours;
    @FXML
    private TextField searchMaxHours;

    @FXML
    private ComboBox<String> sortFieldCombo;
    @FXML
    private RadioButton sortAscRadio;
    @FXML
    private RadioButton sortDescRadio;

    private boolean filtersVisible = false;


    //----------------------------------------------------------------------------------

    private FilteredList<EquipmentDto> filteredData;

    private final EquipmentRepository repository = AppContext.getEquipmentRepository();


    @FXML
    private void initialize() {
        equipmentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        configureTableColumns();
        UserRole role = AppContext.getSessionManager().getRole();
        addEquipmentBtn.setVisible(role == UserRole.ADMIN || role == UserRole.SUPERADMIN);
        ObservableList<EquipmentDto> source = repository.getEquipmentList();
        filteredData = new FilteredList<>(source, p -> true);
        SortedList<EquipmentDto> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(equipmentTable.comparatorProperty());
        equipmentTable.setItems(sortedData);
        equipmentTable.getItems().addListener((ListChangeListener.Change<? extends EquipmentDto> c) -> {
            updateRecordCount();
        });
        initFilterComponents();
        setupFilterListeners();
        initSortControls();
        updateRecordCount();
    }

    private void initFilterComponents() {
        searchCategory.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        FilteredList<EquipmentTypeDto> level6Types = new FilteredList<>(AppContext.getEquipmentTypeRepository().getEquipmentTypesList());
        level6Types.setPredicate(type -> type.getLevel() != null && type.getLevel() == 6);
        searchType.setItems(level6Types);
        searchType.setConverter(new StringConverter<EquipmentTypeDto>() {
            @Override
            public String toString(EquipmentTypeDto type) {
                return type == null ? "" : type.getName();
            }

            @Override
            public EquipmentTypeDto fromString(String string) {
                return null;
            }
        });
        searchEmployee.setItems(AppContext.getUserRepository().getUserList());
        searchDepartment.setItems(AppContext.getDepartmentRepository().getDepartmentsList());
    }

    private void initSortControls() {
        ObservableList<String> sortFields = FXCollections.observableArrayList(
                "Инвентарный номер",
                "Серийный номер",
                "Тип",
                "Наименование",
                "Локация",
                "Подразделение",
                "Сотрудник",
                "Категория",
                "Текущая наработка",
                "Максимальная наработка",
                "Код номенклатуры"
        );
        sortFieldCombo.setItems(sortFields);
    }


    private void setupFilterListeners() {
        searchInvNum.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        searchSerial.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        searchName.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        searchLocation.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        searchFullCode.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        searchType.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        searchCategory.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        searchEmployee.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        searchDepartment.valueProperty().addListener((o, old, n) -> updateFilter());

        onlyFreeCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        onlyBundledCheck.selectedProperty().addListener((obs, oldVal, newVal) -> updateFilter());

        searchCurrentHours.textProperty().addListener((o, old, n) -> updateFilter());
        searchMaxHours.textProperty().addListener((o, old, n) -> updateFilter());
    }

    @FXML
    private void applySorting() {
        String selectedField = sortFieldCombo.getValue();
        if (selectedField == null) return;

        boolean ascending = sortAscRadio.isSelected();
        equipmentTable.getSortOrder().clear();

        TableColumn<EquipmentDto, ?> column = switch (selectedField) {
            case "Инвентарный номер" -> invNumCol;
            case "Серийный номер" -> serialNumCol;
            case "Тип" -> typeCol;
            case "Наименование" -> nameCol;
            case "Локация" -> locationCol;
            case "Подразделение" -> departmentCol;
            case "Сотрудник" -> employeeCol;
            case "Категория" -> categoryCol;
            case "Текущая наработка" -> currentHoursCol;
            case "Максимальная наработка" -> maxHoursCol;
            case "Код номенклатуры" -> fullCodeCol;
            default -> null;
        };

        if (column != null) {
            column.setSortType(ascending ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
            equipmentTable.getSortOrder().add(column);
            equipmentTable.sort();
        }
    }

    private void updateFilter() {
        filteredData.setPredicate(equipment -> {
            boolean match = true;

            if (searchInvNum.getText() != null && !searchInvNum.getText().isBlank()) {
                match = match && equipment.getInventoryNumber() != null &&
                        equipment.getInventoryNumber().toLowerCase().contains(searchInvNum.getText().toLowerCase());
            }
            if (searchSerial.getText() != null && !searchSerial.getText().isBlank()) {
                match = match && equipment.getSerialNumber() != null &&
                        equipment.getSerialNumber().toLowerCase().contains(searchSerial.getText().toLowerCase());
            }
            if (searchName.getText() != null && !searchName.getText().isBlank()) {
                match = match && equipment.getName() != null &&
                        equipment.getName().toLowerCase().contains(searchName.getText().toLowerCase());
            }
            if (searchLocation.getText() != null && !searchLocation.getText().isBlank()) {
                match = match && equipment.getLocation() != null &&
                        equipment.getLocation().toLowerCase().contains(searchLocation.getText().toLowerCase());
            }
            if (searchFullCode.getText() != null && !searchFullCode.getText().isBlank()) {
                match = match && equipment.getType() != null && equipment.getType().getFullCode() != null &&
                        equipment.getType().getFullCode().toLowerCase().contains(searchFullCode.getText().toLowerCase());
            }

            if (searchType.getValue() != null) {
                match = match && equipment.getType() != null &&
                        equipment.getType().getId().equals(searchType.getValue().getId());
            }
            if (searchCategory.getValue() != null) {
                match = match && equipment.getCategory() == searchCategory.getValue();
            }
            if (searchEmployee.getValue() != null) {
                match = match && equipment.getEmployee() != null &&
                        equipment.getEmployee().getId().equals(searchEmployee.getValue().getId());
            }
            if (searchDepartment.getValue() != null) {
                match = match && equipment.getDepartment() != null &&
                        equipment.getDepartment().getId().equals(searchDepartment.getValue().getId());
            }
            if (onlyFreeCheck.isSelected()) {
                match = match && equipment.getEmployee() == null && equipment.getParent() == null;
            }
            if (onlyBundledCheck.isSelected()) {
                match = match && equipment.getParent() != null;
            }
            match &= checkNumeric(equipment.getCurrentOperatingHours(), searchCurrentHours);
            match &= checkNumeric(equipment.getMaxOperatingHours(), searchMaxHours);
            return match;
        });
    }

    private boolean checkNumeric(Integer value, TextField field) {
        String filter = field.getText();
        if (filter == null || filter.isBlank()) return true; // Если фильтр пуст, пропускаем
        if (value == null) return false; // Если в фильтре что-то есть, а у оборудования null -> не подходит

        filter = filter.trim();
        try {
            if (filter.startsWith(">")) {
                return value > Integer.parseInt(filter.substring(1).trim());
            }
            if (filter.startsWith("<")) {
                return value < Integer.parseInt(filter.substring(1).trim());
            }
            if (filter.contains("-")) {
                String[] parts = filter.split("-");
                return value >= Integer.parseInt(parts[0].trim()) && value <= Integer.parseInt(parts[1].trim());
            }
            return value == Integer.parseInt(filter); // Точное совпадение
        } catch (Exception e) {
            return false; // Если ввели (например, буквы), запись скрывается
        }
    }


    @FXML
    private void resetFilters() {
        searchInvNum.clear();
        searchSerial.clear();
        searchName.clear();
        searchLocation.clear();
        searchFullCode.clear();
        searchType.setValue(null);
        searchCategory.setValue(null);
        searchEmployee.setValue(null);
        sortFieldCombo.setValue(null);
        searchDepartment.setValue(null);
        searchCurrentHours.clear();
        searchMaxHours.clear();
        onlyFreeCheck.setSelected(false);
        onlyBundledCheck.setSelected(false);
        equipmentTable.getSortOrder().clear();
        sortAscRadio.setSelected(true);
    }


    private void configureTableColumns() {
        numberCol.setSortable(false);
        numberCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        invNumCol.setCellValueFactory(cellData -> cellData.getValue().getInventoryNumberProperty());
        serialNumCol.setCellValueFactory(cellData -> cellData.getValue().getSerialNumberProperty());
        typeCol.setCellValueFactory(cellData -> cellData.getValue().getType().getNameProperty());
        nameCol.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        locationCol.setCellValueFactory(cellData -> cellData.getValue().getLocationProperty());
        departmentCol.setCellValueFactory(cellData -> {
            DepartmentDto dept = cellData.getValue().getDepartment();
            return new SimpleStringProperty(dept != null && dept.getName() != null ? dept.getName() : "");
        });
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
        currentHoursCol.setCellValueFactory(cellData -> cellData.getValue().getCurrentOperatingHoursProperty());
        maxHoursCol.setCellValueFactory(cellData -> cellData.getValue().getMaxOperatingHoursProperty());
        setupContextMenu();
    }


    private void setupContextMenu() {
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentDto> row = new TableRow<>();
            Runnable buildContextMenu = () -> {

                EquipmentDto item = row.getItem();
                UserRole role = AppContext.getSessionManager().getRole();
                String currentUsername = AppContext.getSessionManager().getUsername();
                boolean isAdmin = role == UserRole.ADMIN || role == UserRole.SUPERADMIN;
                boolean isUser = role == UserRole.USER;

                boolean isOwner = item != null && item.getEmployee() != null &&
                        item.getEmployee().getUsername().equals(currentUsername);

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

                if (item != null) {
                    if (isAdmin || isOwner) {
                        menuItems.add(new SeparatorMenuItem());

                        MenuItem addHoursItem = new MenuItem("Добавить наработку");
                        addHoursItem.setOnAction(e -> loadAddHoursView(item));
                        menuItems.add(addHoursItem);

                        MenuItem historyItem = new MenuItem("История наработки");
                        historyItem.setOnAction(e -> loadHoursHistoryView(item));
                        menuItems.add(historyItem);
                    }
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

    private void updateRecordCount() {
        Platform.runLater(() -> {
            int count = equipmentTable.getItems().size();
            if (recordCountLabel != null) {
                recordCountLabel.setText("Найдено записей: " + count);
            }
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
                param -> new AttachToBundleViewController(selectedItem)
        );
    }

    private void loadEquipmentRemoveFromBundleView(EquipmentDto selectedItem) {
        WindowManager.openModalWindow(
                "bundles/equipment-remove-from-bundle-view.fxml",
                "Вывод из комплекта",
                param -> new DetachFromBundleViewController(selectedItem)
        );
    }

    private void loadAddHoursView(EquipmentDto selectedItem) {
        WindowManager.openModalWindow("crud/add-hours-view.fxml", "Внесение наработки",
                param -> new AddHoursViewController(selectedItem));
    }

    private void loadHoursHistoryView(EquipmentDto selectedItem) {
        WindowManager.openModalWindow("crud/hours-history-view.fxml", "История наработок",
                param -> new HoursHistoryViewController(selectedItem));
    }

    private void loadExportView() {
        WindowManager.openModalWindow("export-view.fxml", "Экспорт оборудования в Excel");
    }

    @FXML
    private void toggleFilterMenu() {
        double panelWidth = 370;
        double targetX = filtersVisible ? panelWidth : 0;

        Interpolator smoothInterpolator = Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(250),
                        new KeyValue(slidingPanel.translateXProperty(), targetX, smoothInterpolator))
        );

        timeline.play();
        filtersVisible = !filtersVisible;
        toggleMenuBtn.setText(filtersVisible ? "▶" : "◀");
    }

    @FXML
    private void handleExportClick() {
        ObservableList<EquipmentDto> selectedItems = equipmentTable.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            NotificationManager.showError("Ошибка экспорта", "Выберите хотя бы одну строку в таблице для экспорта.");
            return;
        }

        WindowManager.openModalWindow(
                "export-view.fxml",
                "Настройки экспорта",
                param -> new ExportViewController(new ArrayList<>(selectedItems))
        );
    }
}