package org.example.tech_ops_gui.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.enums.UserRole;
import org.example.tech_ops_gui.enums.UserStatus;
import org.example.tech_ops_gui.utils.FormatUtil;

import java.time.format.DateTimeFormatter;

public class UserCardController {

    @FXML private Label initialsLabel, militaryRankLabel, fullNameLabel, phoneLabel, usernameLabel, roleLabel, statusLabel, createdLabel;
    @FXML private Button toggleStatusButton, deleteButton, toggleRoleButton;

    private UserDto user;
    private UserActionHandler actionHandler;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void setUser(UserDto user, UserActionHandler handler) {
        this.user = user;
        this.actionHandler = handler;

        String currentUsername = AppContext.getSessionManager().getUsername();
        UserRole currentRole = AppContext.getSessionManager().getRole();

        boolean isSelfSuperAdmin = currentRole == UserRole.SUPERADMIN && currentUsername != null && currentUsername.equals(user.getUsername());
        toggleStatusButton.setVisible(!isSelfSuperAdmin);
        toggleStatusButton.setManaged(!isSelfSuperAdmin);
        deleteButton.setVisible(!isSelfSuperAdmin);
        deleteButton.setManaged(!isSelfSuperAdmin);

        boolean targetIsSuperAdmin = user.getRole() == UserRole.SUPERADMIN;
        boolean canChangeRole = currentRole == UserRole.SUPERADMIN && !isSelfSuperAdmin && !targetIsSuperAdmin;
        toggleRoleButton.setVisible(canChangeRole);
        toggleRoleButton.setManaged(canChangeRole);

        // Используем новую утилиту для всего форматирования!
        fullNameLabel.setText(FormatUtil.buildFullName(user));
        initialsLabel.setText(FormatUtil.getInitials(user));
        phoneLabel.setText(FormatUtil.formatPhone(user.getPhoneNumber()));
        roleLabel.setText(FormatUtil.getRoleDisplayName(user.getRole()));

        militaryRankLabel.setText(user.getMilitaryRank());
        usernameLabel.setText("@" + user.getUsername());
        roleLabel.getStyleClass().add("badge-" + user.getRole().name().toLowerCase());

        updateStatusDisplay();

        if (user.getCreatedAt() != null) {
            createdLabel.setText("Зарегистрирован: " + user.getCreatedAt().format(DATE_FORMATTER));
        }

        updateToggleButton();

        toggleStatusButton.setOnAction(e -> handleToggleStatus());
        toggleRoleButton.setOnAction(e -> handleToggleRole());
        deleteButton.setOnAction(e -> handleDelete());
    }

    private void updateStatusDisplay() {
        statusLabel.setText(FormatUtil.getStatusDisplayName(user.getStatus()));
        statusLabel.getStyleClass().removeAll("badge-active", "badge-blocked");
        statusLabel.getStyleClass().add(user.getStatus() == UserStatus.ACTIVE ? "badge-active" : "badge-blocked");
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

    private void handleToggleStatus() {
        if (actionHandler != null) actionHandler.onToggleStatus(user, user.getStatus() == UserStatus.ACTIVE ? UserStatus.BLOCKED : UserStatus.ACTIVE);
    }

    private void handleToggleRole() {
        if(actionHandler != null) actionHandler.onToggleRole(user, user.getRole() == UserRole.USER ? UserRole.ADMIN : UserRole.USER);
    }

    private void handleDelete() {
        if (actionHandler != null) actionHandler.onDelete(user);
    }

    public interface UserActionHandler {
        void onToggleStatus(UserDto user, UserStatus newStatus);
        void onDelete(UserDto user);
        void onToggleRole(UserDto user, UserRole newRole);
    }
}