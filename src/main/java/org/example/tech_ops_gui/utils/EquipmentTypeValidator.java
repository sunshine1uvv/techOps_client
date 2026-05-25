package org.example.tech_ops_gui.utils;

import org.example.tech_ops_gui.dto.EquipmentTypeDto;

import java.util.ArrayList;
import java.util.List;

public class EquipmentTypeValidator {

    public static List<String> validate(EquipmentTypeDto dto) {
        List<String> errors = new ArrayList<>();

        Integer level = dto.getLevel();
        if (level == null || level < 1 || level > 6) {
            errors.add("• Необходимо выбрать корректный уровень оборудования (от 1 до 6)");
        }

        String name = dto.getName();
        if (name == null || name.trim().isEmpty()) {
            errors.add("• Наименование обязательно для заполнения");
        } else if (name.trim().length() > 255) {
            errors.add("• Наименование не должно превышать 255 символов");
        }

        if (level != null && level > 1 && dto.getParent() == null) {
            errors.add("• Для уровней 2-6 необходимо выбрать родительскую категорию");
        }

        String code = dto.getCode();
        if (code == null || code.trim().isEmpty()) {
            errors.add("• Код обязателен для заполнения");
        } else if (level != null) {
            String trimmedCode = code.trim();
            if (level == 6) {
                // Строго 4 цифры для 6 уровня
                if (!trimmedCode.matches("\\d{4}")) {
                    errors.add("• Код для 6-го уровня должен состоять ровно из 4 цифр (например, 0001)");
                }
            } else {
                // Строго 2 цифры для 1-5 уровней
                if (!trimmedCode.matches("\\d{2}")) {
                    errors.add("• Код для уровней 1-5 должен состоять ровно из 2 цифр (например, 01 или 12)");
                }
            }
        }

        return errors;
    }
}