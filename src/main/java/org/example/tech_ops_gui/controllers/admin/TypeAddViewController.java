package org.example.tech_ops_gui.controllers.admin;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentTypeDto;
import org.example.tech_ops_gui.repository.EquipmentTypeRepository;
import org.example.tech_ops_gui.utils.EquipmentTypeValidator;
import org.example.tech_ops_gui.utils.NotificationManager;

import java.util.List;
import java.util.stream.Collectors;

public class TypeAddViewController {

    @FXML private TreeView<EquipmentTypeDto> equipmentTypeTreeView;
    @FXML private ComboBox<Integer> levelComboBox;
    @FXML private SearchableComboBox<EquipmentTypeDto> parentComboBox;
    @FXML private TextField nameField;
    @FXML private TextField codeField;
    @FXML private TextField fullCodeField;

    private final EquipmentTypeRepository equipmentTypeRepository = AppContext.getEquipmentTypeRepository();

    private final ListChangeListener<EquipmentTypeDto> repositoryListener = c -> {
        Platform.runLater(this::populateTreeView);
    };

    @FXML
    private void initialize() {
        levelComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));

        parentComboBox.setConverter(new StringConverter<EquipmentTypeDto>() {
            @Override
            public String toString(EquipmentTypeDto object) {
                return object == null ? "" : object.getName() + " (" + object.getFullCode() + ")";
            }

            @Override
            public EquipmentTypeDto fromString(String string) {
                return null;
            }
        });

        // Настройка кастомного отображения элементов в дереве
        setupTreeView();

        // Построение дерева
        populateTreeView();

        // Слушаем изменения в репозитории, чтобы дерево обновлялось динамически
        equipmentTypeRepository.getEquipmentTypesList().addListener(repositoryListener);

        // Удобство: клик по дереву автоматически выбирает уровень и родителя в форме
        equipmentTypeTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null && newVal.getValue().getId() != null) {
                EquipmentTypeDto selectedNode = newVal.getValue();
                int currentLevel = selectedNode.getLevel() != null ? selectedNode.getLevel() : 1;

                // Если мы не на последнем 6-м уровне, можем предложить добавить дочерний
                if (currentLevel < 6) {
                    levelComboBox.setValue(currentLevel + 1);
                    // Важно обновить список родителей перед установкой значения,
                    // слушатель уровня сделает это автоматически, нам остается только задать parent
                    Platform.runLater(() -> parentComboBox.setValue(selectedNode));
                }
            }
        });

        // Слушатель выбора уровня оборудования
        levelComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            if (newVal == 1) {
                parentComboBox.setDisable(true);
                parentComboBox.setValue(null);
                // Исправление NPE: передаем пустой список вместо null
                parentComboBox.setItems(FXCollections.observableArrayList());

                // Меняем подсказку, когда комбобокс заблокирован
                parentComboBox.setPromptText("Для 1 уровня родитель не нужен");
            } else {
                parentComboBox.setDisable(false);

                // Меняем подсказку на призыв к действию
                parentComboBox.setPromptText("Выберите родительскую категорию...");

                int requiredParentLevel = newVal - 1;

                ObservableList<EquipmentTypeDto> allTypes = equipmentTypeRepository.getEquipmentTypesList();

                List<EquipmentTypeDto> filteredParents = allTypes.stream()
                        .filter(t -> t.getLevel() != null && t.getLevel() == requiredParentLevel)
                        .collect(Collectors.toList());

                parentComboBox.setItems(FXCollections.observableArrayList(filteredParents));
            }

            if (newVal == 6) {
                codeField.setPromptText("Например: 0001 (только 4 цифры)");
            } else {
                codeField.setPromptText("Например: 01 (только 2 цифры)");
            }

            updateFullCode();
        });
    }

    private void setupTreeView() {
        equipmentTypeTreeView.setCellFactory(tv -> new TreeCell<EquipmentTypeDto>() {
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

        // Создаем фиктивный корень
        EquipmentTypeDto rootDto = new EquipmentTypeDto();
        rootDto.setName("Все типы оборудования");
        // fullCode по умолчанию null
        TreeItem<EquipmentTypeDto> rootItem = new TreeItem<>(rootDto);
        rootItem.setExpanded(true);

        // СТРОГО берем только элементы 1-го уровня.
        // Избегаем проверки на getParent() == null, так как бэкенд может присылать его пустым для всех
        List<EquipmentTypeDto> level1Types = allTypes.stream()
                .filter(t -> t.getLevel() != null && t.getLevel() == 1)
                .collect(Collectors.toList());

        for (EquipmentTypeDto l1 : level1Types) {
            rootItem.getChildren().add(createTreeItem(l1, allTypes));
        }

        equipmentTypeTreeView.setRoot(rootItem);
    }

    private TreeItem<EquipmentTypeDto> createTreeItem(EquipmentTypeDto currentItem, List<EquipmentTypeDto> allTypes) {
        TreeItem<EquipmentTypeDto> treeItem = new TreeItem<>(currentItem);
        // Разворачиваем 1 и 2 уровни для удобства
        treeItem.setExpanded(currentItem.getLevel() != null && currentItem.getLevel() < 3);

        int currentLevel = currentItem.getLevel() != null ? currentItem.getLevel() : 1;

        List<EquipmentTypeDto> children = allTypes.stream()
                .filter(t -> {
                    // 1. Обязательно проверяем, что это строго следующий уровень
                    if (t.getLevel() == null || t.getLevel() != currentLevel + 1) {
                        return false;
                    }

                    // 2. Способ А: Если бэкенд честно отдает поле parent
                    if (t.getParent() != null && t.getParent().getId() != null && currentItem.getId() != null) {
                        return t.getParent().getId().equals(currentItem.getId());
                    }

                    // 3. Способ Б (Надежный фоллбэк): Если parent = null,
                    // связываем дочерние элементы по полному коду.
                    // Код ребенка (08.01.01) всегда начинается с кода родителя (08.01)
                    if (t.getFullCode() != null && currentItem.getFullCode() != null) {
                        return t.getFullCode().startsWith(currentItem.getFullCode());
                    }

                    return false;
                })
                .collect(Collectors.toList());

        // Рекурсивно добавляем найденных детей
        for (EquipmentTypeDto child : children) {
            treeItem.getChildren().add(createTreeItem(child, allTypes));
        }

        return treeItem;
    }

    private void updateFullCode() {
        String currentCode = codeField.getText() == null ? "" : codeField.getText().trim().toUpperCase();
        Integer level = levelComboBox.getValue();

        if (level != null && level > 1) {
            EquipmentTypeDto parent = parentComboBox.getValue();
            if (parent != null && parent.getFullCode() != null) {
                String separator = (level == 6) ? "/" : ".";
                fullCodeField.setText(parent.getFullCode() + separator + currentCode);
            } else {
                fullCodeField.setText(currentCode);
            }
        } else {
            fullCodeField.setText(currentCode);
        }
    }

    @FXML
    private void handleSave() {
        Integer selectedLevel = levelComboBox.getValue();
        EquipmentTypeDto selectedParent = parentComboBox.getValue();
        String name = nameField.getText();
        String code = codeField.getText();
        String fullCode = fullCodeField.getText();

        EquipmentTypeDto newType = new EquipmentTypeDto();
        newType.setLevel(selectedLevel);
        newType.setName(name == null ? "" : name.trim());
        newType.setCode(code == null ? "" : code.trim().toUpperCase());
        newType.setFullCode(fullCode);

        if (selectedLevel != null && selectedLevel > 1) {
            newType.setParent(selectedParent);
        }

        List<String> validationErrors = EquipmentTypeValidator.validate(newType);

        if (!validationErrors.isEmpty()) {
            String errorMessage = String.join("\n", validationErrors);
            NotificationManager.showError("Ошибка валидации", errorMessage);
            return;
        }

        try {
            equipmentTypeRepository.save(newType).thenRun(() -> {
                Platform.runLater(() -> {
                    NotificationManager.showInfo("Успешно", "Тип оборудования [" + fullCode + "] успешно добавлен.");
                    closeWindow();
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    NotificationManager.showError("Ошибка сервера", "Не удалось сохранить тип оборудования.");
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
        Stage stage = (Stage) levelComboBox.getScene().getWindow();
        stage.close();
    }
}