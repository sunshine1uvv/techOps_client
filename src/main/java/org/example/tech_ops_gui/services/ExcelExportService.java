package org.example.tech_ops_gui.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelExportService {

    public void exportEquipment(File file, List<EquipmentDto> items, String sheetName, List<String> selectedColumns, boolean appendMode) {
        Workbook workbook = null;
        FileInputStream fis = null;

        try {
            if (appendMode && file.exists()) {
                fis = new FileInputStream(file);
                workbook = WorkbookFactory.create(fis);
            } else {
                workbook = new XSSFWorkbook();
            }

            Sheet sheet = workbook.getSheet(sheetName);
            boolean isNewSheet = (sheet == null);
            if (isNewSheet) {
                sheet = workbook.createSheet(sheetName.isEmpty() ? "Лист1" : sheetName);
            }

            int rowNum = isNewSheet ? 0 : sheet.getLastRowNum() + 1;

            if (isNewSheet) {
                Row headerRow = sheet.createRow(rowNum++);
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                for (int i = 0; i < selectedColumns.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(selectedColumns.get(i));
                    cell.setCellStyle(headerStyle);
                }
            }

            for (EquipmentDto item : items) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < selectedColumns.size(); i++) {
                    Cell cell = row.createCell(i);
                    String columnName = selectedColumns.get(i);

                    switch (columnName) {
                        case "Инвентарный номер" -> cell.setCellValue(item.getInventoryNumber() != null ? item.getInventoryNumber() : "");
                        case "Серийный номер" -> cell.setCellValue(item.getSerialNumber() != null ? item.getSerialNumber() : "");
                        case "Тип" -> cell.setCellValue(item.getType() != null ? item.getType().getName() : "");
                        case "Наименование" -> cell.setCellValue(item.getName() != null ? item.getName() : "");
                        case "Местоположение" -> cell.setCellValue(item.getLocation() != null ? item.getLocation() : "");
                        case "Подразделение" -> cell.setCellValue(item.getDepartment() != null ? item.getDepartment().getName() : "");
                        case "Сотрудник" -> cell.setCellValue(item.getEmployee() != null ? item.getEmployee().getSurname() : "");
                        case "Категория" -> cell.setCellValue(item.getCategory() != null ? String.valueOf(item.getCategory()) : "");
                        case "Комплект" -> cell.setCellValue(item.getParent() != null ? "Да" : "Нет")   ;
                        case "Текущая наработка" -> cell.setCellValue(item.getCurrentOperatingHours() != null ? String.valueOf(item.getCurrentOperatingHours()) : "");
                        case "Максимальная наработка" -> cell.setCellValue(item.getMaxOperatingHours() != null ? String.valueOf(item.getMaxOperatingHours()) : "");
                        case "Код номенклатуры" -> cell.setCellValue(item.getType() != null && item.getType().getFullCode() != null ? item.getType().getFullCode() : "");
                    }
                }
            }

            for (int i = 0; i < selectedColumns.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            if (fis != null) fis.close();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            NotificationManager.showInfo("Успех", "Данные успешно экспортированы в Excel!");

        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.showError("Ошибка экспорта", "Произошла ошибка при сохранении: " + e.getMessage());
        } finally {
            try {
                if (workbook != null) workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}