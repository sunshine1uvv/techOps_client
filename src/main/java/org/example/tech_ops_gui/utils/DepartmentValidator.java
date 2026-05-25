package org.example.tech_ops_gui.utils;

import org.example.tech_ops_gui.dto.DepartmentDto;

import java.util.ArrayList;
import java.util.List;

public class DepartmentValidator {

    public static List<String> validate(DepartmentDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            errors.add("• Название подразделения не может быть пустым");
        } else if (dto.getName().length() > 255) {
            errors.add("• Название слишком длинное (макс. 255 символов)");
        }

        return errors;
    }
}
