package org.example.tech_ops_gui.controllers.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.repository.EquipmentTypeRepository;
import org.example.tech_ops_gui.utils.NotificationManager;

import java.util.List;
import java.util.stream.Collectors;

public class TypeDeleteViewController {

    @FXML private TreeView<EquipmentTypeDto> typeTreeView;
    @FXML private SearchableComboBox<EquipmentTypeDto> typeComboBox;

    private final EquipmentTypeRepository equipmentTypeRepository = AppContext.getEquipmentTypeRepository();

    private final ListChangeListener<EquipmentTypeDto> repositoryListener = c -> {
        Platform.runLater(() -> {
            populateTreeView();
            updateComboBoxItems();
        });
    };

    @FXML
    private void initialize() {
        // 1. Настраиваем ComboBox
        updateComboBoxItems();
        typeComboBox.setConverter(new StringConverter<EquipmentTypeDto>() {
            @Override
            public String toString(EquipmentTypeDto object) {
                return object == null || object.getId() == null ? "" : object.getName() + " (" + object.getFullCode() + ")";
            }

            @Override
            public EquipmentTypeDto fromString(String string) {
                return null;
            }
        });


        setupTreeView();
        populateTreeView();

        equipmentTypeRepository.getEquipmentTypesList().addListener(repositoryListener);

        typeTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null && newVal.getValue().getId() != null) {
                Platform.runLater(() -> typeComboBox.getSelectionModel().select(newVal.getValue()));
            }
        });
    }

    private void updateComboBoxItems() {
        typeComboBox.setItems(FXCollections.observableArrayList(equipmentTypeRepository.getEquipmentTypesList()));
    }

    private void setupTreeView() {
        typeTreeView.setCellFactory(tv -> new TreeCell<EquipmentTypeDto>() {
            @Override
            protected void updateItem(EquipmentTypeDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item.getFullCode() == null || item.getFullCode().isEmpty()) {
                        setText(item.getName());
                    } else {
                        setText("[" + item.getFullCode() + "] " + item.getName());
                    }
                }
            }
        });
    }

    private void populateTreeView() {
        ObservableList<EquipmentTypeDto> allTypes = equipmentTypeRepository.getEquipmentTypesList();

        EquipmentTypeDto rootDto = new EquipmentTypeDto();
        rootDto.setName("Все типы оборудования");
        TreeItem<EquipmentTypeDto> rootItem = new TreeItem<>(rootDto);
        rootItem.setExpanded(true);

        List<EquipmentTypeDto> level1Types = allTypes.stream()
                .filter(t -> t.getLevel() != null && t.getLevel() == 1)
                .collect(Collectors.toList());

        for (EquipmentTypeDto l1 : level1Types) {
            rootItem.getChildren().add(createTreeItem(l1, allTypes));
        }

        typeTreeView.setRoot(rootItem);
    }

    private TreeItem<EquipmentTypeDto> createTreeItem(EquipmentTypeDto currentItem, List<EquipmentTypeDto> allTypes) {
        TreeItem<EquipmentTypeDto> treeItem = new TreeItem<>(currentItem);
        treeItem.setExpanded(currentItem.getLevel() != null && currentItem.getLevel() < 3);

        int currentLevel = currentItem.getLevel() != null ? currentItem.getLevel() : 1;

        List<EquipmentTypeDto> children = allTypes.stream()
                .filter(t -> {
                    if (t.getLevel() == null || t.getLevel() != currentLevel + 1) return false;

                    if (t.getParent() != null && t.getParent().getId() != null && currentItem.getId() != null) {
                        return t.getParent().getId().equals(currentItem.getId());
                    }
                    if (t.getFullCode() != null && currentItem.getFullCode() != null) {
                        return t.getFullCode().startsWith(currentItem.getFullCode());
                    }
                    return false;
                })
                .collect(Collectors.toList());

        for (EquipmentTypeDto child : children) {
            treeItem.getChildren().add(createTreeItem(child, allTypes));
        }

        return treeItem;
    }

    /**
     * ЛОГИКА ПРОВЕРКИ: Проверяем, есть ли в базе элементы, ссылающиеся на удаляемый.
     */
    private boolean hasChildTypes(EquipmentTypeDto typeToDelete) {
        if (typeToDelete == null || typeToDelete.getId() == null) return false;

        return equipmentTypeRepository.getEquipmentTypesList().stream()
                .anyMatch(t -> {
                    if (t.getParent() != null && t.getParent().getId() != null) {
                        if (t.getParent().getId().equals(typeToDelete.getId())) {
                            return true;
                        }
                    }

                    if (t.getFullCode() != null && typeToDelete.getFullCode() != null) {
                        String separator = (typeToDelete.getLevel() != null && typeToDelete.getLevel() == 5) ? "/" : ".";
                        if (t.getFullCode().startsWith(typeToDelete.getFullCode() + separator)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    @FXML
    private void handleDelete() {
        // Мы берем значение именно из ComboBox, так как пользователь мог найти тип вводом текста
        EquipmentTypeDto selectedType = typeComboBox.getValue();

        // 1. Валидация выбора
        if (selectedType == null || selectedType.getId() == null) {
            NotificationManager.showError("Ошибка", "Пожалуйста, выберите тип оборудования для удаления.");
            return;
        }

        // 2. СТРОГАЯ ПРОВЕРКА НА ДОЧЕРНИЕ ЭЛЕМЕНТЫ
        if (hasChildTypes(selectedType)) {
            NotificationManager.showError("Запрещено",
                    "Нельзя удалить категорию [" + selectedType.getName() + "], так как она содержит вложенные подтипы. Сначала удалите их.");
            return;
        }

        // 3. Отправка запроса на сервер
        try {
            equipmentTypeRepository.delete(selectedType.getId()).thenRun(() -> {
                Platform.runLater(() -> {
                    NotificationManager.showInfo("Успешно", "Тип оборудования [" + selectedType.getFullCode() + "] удален.");
                    typeComboBox.getSelectionModel().clearSelection();
                    closeWindow();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    NotificationManager.showError("Невозможно удалить", "Ошибка базы данных. К этому типу привязана реальная техника.");
                });
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        equipmentTypeRepository.getEquipmentTypesList().removeListener(repositoryListener);
        Stage stage = (Stage) typeComboBox.getScene().getWindow();
        stage.close();
    }
}