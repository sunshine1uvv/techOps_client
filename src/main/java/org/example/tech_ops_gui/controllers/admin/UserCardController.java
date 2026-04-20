package org.example.tech_ops_gui.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.entities.UserRole;
import org.example.tech_ops_gui.entities.UserStatus;
import org.example.tech_ops_gui.utils.SessionManager;


import java.time.format.DateTimeFormatter;

public class UserCardController {

    @FXML private Label initialsLabel;
    @FXML private Label militaryRankLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label statusLabel;
    @FXML private Label createdLabel;
    @FXML private Button toggleStatusButton;
    @FXML private Button deleteButton;
    @FXML private Button toggleRoleButton;

    private UserDto user;
    private UserActionHandler actionHandler;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void setUser(UserDto user, UserActionHandler handler) {
        this.user = user;
        this.actionHandler = handler;

        String currentUsername = SessionManager.getInstance().getUsername();
        String currentRole = SessionManager.getInstance().getRole();

        boolean isSelfSuperAdmin = "SUPERADMIN".equals(currentRole)
                && currentUsername != null
                && currentUsername.equals(user.getUsername());
        toggleStatusButton.setVisible(!isSelfSuperAdmin);
        toggleStatusButton.setManaged(!isSelfSuperAdmin);
        deleteButton.setVisible(!isSelfSuperAdmin);
        deleteButton.setManaged(!isSelfSuperAdmin);

        boolean targetIsSuperAdmin = user.getRole() == UserRole.SUPERADMIN;
        boolean canChangeRole = "SUPERADMIN".equals(currentRole)
                && !isSelfSuperAdmin
                && !targetIsSuperAdmin;

        toggleRoleButton.setVisible(canChangeRole);
        toggleRoleButton.setManaged(canChangeRole);

        String fullName = String.format("%s %s %s",
                user.getSurname(),
                user.getName(),
                user.getPatronymic() != null ? user.getPatronymic() : "");
        fullNameLabel.setText(fullName);

        String initials = (user.getName().charAt(0) + "" + user.getSurname().charAt(0)).toUpperCase();
        initialsLabel.setText(initials);

        militaryRankLabel.setText(user.getMilitaryRank());
        phoneLabel.setText(formatPhone(user.getPhoneNumber()));
        usernameLabel.setText("@" + user.getUsername());

        roleLabel.setText(getRoleDisplayName(user.getRole()));
        roleLabel.getStyleClass().add("badge-" + user.getRole().name().toLowerCase());

        updateStatusDisplay();

        if (user.getCreatedAt() != null) {
            createdLabel.setText("Зарегистрирован: " + user.getCreatedAt().format(DATE_FORMATTER));
        }

        updateToggleButton();
        System.out.println("User: " + user.getUsername() + ", role: " + user.getRole());

        toggleStatusButton.setOnAction(e -> handleToggleStatus());
        toggleRoleButton.setOnAction(e -> handleToggleRole());
        deleteButton.setOnAction(e -> handleDelete());
    }

    private void updateStatusDisplay() {
        String statusText = user.getStatus() == UserStatus.ACTIVE ? "Активен" : "Заблокирован";
        statusLabel.setText(statusText);
        statusLabel.getStyleClass().removeAll("badge-active", "badge-blocked");
        statusLabel.getStyleClass().add(
                user.getStatus() == UserStatus.ACTIVE ? "badge-active" : "badge-blocked"
        );
    }

    private void updateToggleButton() {
        if (user.getStatus() == UserStatus.ACTIVE) {
            toggleStatusButton.setText("🔒 Заблокировать");
            toggleStatusButton.getStyleClass().remove("unblock-button");
        } else {
            toggleStatusButton.setText("🔓 Разблокировать");
            toggleStatusButton.getStyleClass().add("unblock-button");
        }
    }

    private String getRoleDisplayName(UserRole role) {
        return switch (role) {
            case ADMIN -> "Администратор";
            case USER -> "Пользователь";
            case SUPERADMIN -> "Главный администратор";
        };
    }

    private String formatPhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 12 && digits.startsWith("375")) {
            return String.format("+%s (%s) %s-%s-%s",
                    digits.substring(0, 3),
                    digits.substring(3, 5),
                    digits.substring(5, 8),
                    digits.substring(8, 10),
                    digits.substring(10));
        }
        return phone;
    }

    private void handleToggleStatus() {
        if (actionHandler != null) {
            UserStatus newStatus = user.getStatus() == UserStatus.ACTIVE
                    ? UserStatus.BLOCKED : UserStatus.ACTIVE;
            actionHandler.onToggleStatus(user, newStatus);
        }
    }

    private void handleToggleRole() {
        if(actionHandler != null) {
            UserRole newRole = user.getRole() == UserRole.USER ? UserRole.ADMIN : UserRole.USER;
            actionHandler.onToggleRole(user, newRole);
        }
    }

    private void handleDelete() {
        if (actionHandler != null) {
            actionHandler.onDelete(user);
        }
    }

    public interface UserActionHandler {
        void onToggleStatus(UserDto user, UserStatus newStatus);
        void onDelete(UserDto user);
        void onToggleRole(UserDto user, UserRole newRole);
    }
}