package org.example.tech_ops_gui.utils;

import org.example.tech_ops_gui.dto.RegistrationRequestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z]+_[A-Z]{1,2}$");

    public static List<String> validateRegistration(RegistrationRequestDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.getUsername() == null || !USERNAME_PATTERN.matcher(dto.getUsername()).matches()) {
            errors.add("• Логин должен быть в формате Фамилия_Инициалы на латинице (например: Ivanov_II). Цифры запрещены, инициалы пишутся заглавными буквами");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 5) {
            errors.add("• Пароль должен быть не короче 5 символов");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            errors.add("• Имя обязательно для заполнения");
        }
        if (dto.getSurname() == null || dto.getSurname().isBlank()) {
            errors.add("• Фамилия обязательна для заполнения");
        }
        if (dto.getMilitaryRank() == null) {
            errors.add("• Воинское звание обязательно для выбора");
        }

        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank()) {
            errors.add("• Номер телефона обязателен для заполнения");
        } else if (!dto.getPhoneNumber().matches("^\\+375\\d{9}$")) {
            errors.add("• Неверный формат. Ожидается номер в формате +375XXXXXXXXX");
        }
        return errors;
    }
}