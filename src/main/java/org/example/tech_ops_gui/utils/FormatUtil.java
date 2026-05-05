package org.example.tech_ops_gui.utils;

import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.enums.UserRole;
import org.example.tech_ops_gui.enums.UserStatus;

public class FormatUtil {

    public static String buildFullName(UserDto user) {
        if (user == null) return "";
        return String.format("%s %s %s",
                user.getSurname() != null ? user.getSurname() : "",
                user.getName() != null ? user.getName() : "",
                user.getPatronymic() != null ? user.getPatronymic() : "").trim();
    }

    public static String getInitials(UserDto user) {
        if (user == null || user.getName() == null || user.getSurname() == null) return "";
        if (user.getName().isEmpty() || user.getSurname().isEmpty()) return "";
        return (user.getName().charAt(0) + "" + user.getSurname().charAt(0)).toUpperCase();
    }

    public static String formatPhone(String phone) {
        if (phone == null || phone.isBlank()) return "Не указан";
        String digits = phone.replaceAll("[^0-9]", "");
        // Форматирование для белорусских номеров (например: +375 (29) 123-45-67)
        if (digits.length() == 12 && digits.startsWith("375")) {
            return String.format("+%s (%s) %s-%s-%s",
                    digits.substring(0, 3), digits.substring(3, 5),
                    digits.substring(5, 8), digits.substring(8, 10), digits.substring(10));
        }
        return phone;
    }

    public static String getRoleDisplayName(UserRole role) {
        if (role == null) return "";
        return switch (role) {
            case ADMIN -> "Администратор";
            case USER -> "Пользователь";
            case SUPERADMIN -> "Главный администратор";
        };
    }

    public static String getStatusDisplayName(UserStatus status) {
        if (status == null) return "";
        return switch (status) {
            case ACTIVE -> "Активен";
            case BLOCKED -> "Заблокирован";
        };
    }
}