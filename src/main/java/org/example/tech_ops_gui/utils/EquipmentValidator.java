package org.example.tech_ops_gui.utils;

import org.example.tech_ops_gui.dto.EquipmentDto;

import java.util.ArrayList;
import java.util.List;

public class EquipmentValidator {

    public static List<String> validate(EquipmentDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getType() == null) {
            errors.add("• Необходимо выбрать тип оборудования");
        }

        String name = dto.getName();
        if (name != null && name.trim().length() > 255) {
            errors.add("• Название не должно превышать 255 символов");
        }

        String invNum = dto.getInventoryNumber();
        if (invNum != null && !invNum.isBlank() && !invNum.trim().matches("^ИТ\\d{5}$")) {
            errors.add("• Инвентарный номер должен быть в формате 'ИТ' и 5 цифр (например, ИТ00123)");
        }

        String serial = dto.getSerialNumber();
        if (serial != null && serial.trim().length() > 30) {
            errors.add("• Серийный номер не может превышать 30 символов");
        }

        String location = dto.getLocation();
        if (location == null || location.isBlank()) {
            errors.add("• Местоположение обязательно для заполнения");
        } else if (location.trim().length() > 255) {
            errors.add("• Местоположение не должно превышать 255 символов");
        }

        Integer category = dto.getCategory();
        if (category == null) {
            errors.add("• Категория обязательна для выбора.");
        } else if (category < 1 || category > 5) {
            errors.add("• Категория должна быть числом от 1 до 5.");
        }

        Integer maxHours = dto.getMaxOperatingHours();
        if (maxHours != null && maxHours <= 0) {
            errors.add("• Максимальная наработка должна быть больше 0");
        }

        return errors;
    }
}
