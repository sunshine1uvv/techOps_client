package org.example.tech_ops_gui.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.services.ExcelExportService;
import org.example.tech_ops_gui.utils.FileSelectionUtil;
import org.example.tech_ops_gui.utils.WindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportViewController {

    @FXML private RadioButton newFileRadio;
    @FXML private RadioButton existingFileRadio;
    @FXML private VBox newFileBox;
    @FXML private HBox existingFileBox;
    @FXML private TextField newFileNameField;
    @FXML private TextField existingFilePathField;
    @FXML private FlowPane columnsBox;

    private final List<EquipmentDto> itemsToExport; // Данные для экспорта
    private File selectedFile;
    private final List<CheckBox> checkBoxes = new ArrayList<>();

    private final String[] columns = {
            "Инвентарный номер", "Серийный номер", "Тип", "Наименование",
            "Местоположение", "Подразделение", "Сотрудник", "Категория",
            "Комплект", "Текущая наработка", "Максимальная наработка", "Код номенклатуры"
    };


    public ExportViewController(List<EquipmentDto> itemsToExport) {
        this.itemsToExport = itemsToExport;
    }

    @FXML
    public void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm");
        String currentDateTime = LocalDateTime.now().format(formatter);

        newFileNameField.setText("Учет_Оборудования_" + currentDateTime);

        ToggleGroup group = new ToggleGroup();
        newFileRadio.setToggleGroup(group);
        existingFileRadio.setToggleGroup(group);

        existingFileBox.visibleProperty().bind(existingFileRadio.selectedProperty());
        existingFileBox.managedProperty().bind(existingFileRadio.selectedProperty());
        newFileBox.visibleProperty().bind(newFileRadio.selectedProperty());
        newFileBox.managedProperty().bind(newFileRadio.selectedProperty());

        for (String col : columns) {
            CheckBox cb = new CheckBox(col);
            cb.setSelected(true);
            checkBoxes.add(cb);
            columnsBox.getChildren().add(cb);
        }
    }

    @FXML
    private void handleBrowseFile() {
        File file = FileSelectionUtil.chooseFileForOpen(
                existingFilePathField.getScene().getWindow(),
                "Выберите существующий файл",
                "Excel Files", "*.xlsx"
        );
        if (file != null) {
            selectedFile = file;
            existingFilePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleExport(ActionEvent event) {
        File targetFile;

        if (existingFileRadio.isSelected()) {
            if (selectedFile == null) {
                existingFilePathField.setStyle("-fx-border-color: red;");
                return;
            }
            targetFile = selectedFile;
        } else {
            String fileName = newFileNameField.getText().trim();
            if (fileName.isEmpty()) {
                newFileNameField.setStyle("-fx-border-color: red;");
                return;
            }
            if (!fileName.endsWith(".xlsx")) fileName += ".xlsx";

            targetFile = FileSelectionUtil.chooseFileForSave(
                    newFileNameField.getScene().getWindow(),
                    "Сохранить как",
                    fileName,
                    "Excel Files", "*.xlsx"
            );
        }

        if (targetFile != null) {
            ExcelExportService exportService = new ExcelExportService();
            exportService.exportEquipment(
                    targetFile,
                    itemsToExport,
                    "Оборудование",
                    getSelectedColumns(),
                    existingFileRadio.isSelected()
            );
            WindowManager.close(event);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        WindowManager.close(event);
    }

    private List<String> getSelectedColumns() {
        List<String> selected = new ArrayList<>();
        for (CheckBox cb : checkBoxes) if (cb.isSelected()) selected.add(cb.getText());
        return selected;
    }
}