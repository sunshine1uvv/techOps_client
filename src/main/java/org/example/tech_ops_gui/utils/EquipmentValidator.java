package org.example.tech_ops_gui.utils;

import org.example.tech_ops_gui.entities.EquipmentType;

import java.util.ArrayList;
import java.util.List;

public class EquipmentValidator {

    public static List<String> getValidationErrors(EquipmentType type, String name, String invNum,
                                                   String serialNum, String category, String location) {
        List<String> errors = new ArrayList<>();

        if (type == null) errors.add("• Не выбран тип устройства");
        if (location == null || location.isBlank()) {
            errors.add("• Локация обязательна для заполнения");
        }
        if (category == null || !category.matches("^[1-5]$")) {
            errors.add("• Категория должна быть числом от 1 до 5");
        }
        if (invNum != null && !invNum.isBlank() && !invNum.trim().matches("^ИТ\\d{5}$")) {
            errors.add("• Инвентарный номер должен быть в формате ИТXXXXX (5 цифр)");
        }
        if (serialNum != null && serialNum.trim().length() > 30) {
            errors.add("• Серийный номер не может быть длиннее 30 символов");
        }
        if (name != null && name.trim().length() > 255) {
            errors.add("• Название слишком длинное (макс. 255)");
        }
        return errors;
    }

}
