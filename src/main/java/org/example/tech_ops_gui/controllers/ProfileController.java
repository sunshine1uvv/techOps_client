package org.example.tech_ops_gui.controllers;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.dto.UserDto;
import org.example.tech_ops_gui.enums.UserRole;
import org.example.tech_ops_gui.enums.UserStatus;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.repository.UserRepository;
import org.example.tech_ops_gui.utils.SessionManager;

import java.time.format.DateTimeFormatter;

public class ProfileController {

    @FXML
    private TabPane profileTabPane;

    // ---------- Личные данные ----------
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label rankLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label createdAtLabel;

    // ---------- Таблица оборудования ----------
    @FXML
    private TableView<EquipmentDto> equipmentTable;
    @FXML
    private TableColumn<EquipmentDto, String> invNumCol;
    @FXML
    private TableColumn<EquipmentDto, String> serialNumCol;
    @FXML
    private TableColumn<EquipmentDto, String> typeCol;
    @FXML
    private TableColumn<EquipmentDto, String> nameCol;
    @FXML
    private TableColumn<EquipmentDto, String> locationCol;
    @FXML
    private TableColumn<EquipmentDto, Integer> categoryCol;
    @FXML
    private TableColumn<EquipmentDto, String> isBundledCol;
    @FXML
    private TableColumn<EquipmentDto, String> fullCodeCol;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final UserRepository userRepository = AppContext.getUserRepository();
    private final EquipmentRepository equipmentRepository = AppContext.getEquipmentRepository();

    private UserDto currentUser;
    private FilteredList<EquipmentDto> userEquipmentFiltered;
    private final ListChangeListener<UserDto> userListListener = change -> {
        Platform.runLater(() -> {
            if (currentUser == null) return;
            UserDto updated = userRepository.getUserList().stream()
                    .filter(u -> u.getId().equals(currentUser.getId()))
                    .findFirst()
                    .orElse(null);
            if (updated != null) {
                currentUser = updated;
                setUser(currentUser);
                updateEquipmentFilter();
            }
        });
    };

    @FXML
    public void initialize() {
        configureTableColumns();
        setupEquipmentTable();
        loadUserAndEquipment();
        userRepository.getUserList().addListener((userListListener));

        Platform.runLater(() -> {
            if (profileTabPane.getScene() != null && profileTabPane.getScene().getWindow() != null) {
                Stage stage = (Stage) profileTabPane.getScene().getWindow();
                stage.setOnHidden(event -> {
                    userRepository.getUserList().removeListener(userListListener);
                });
            }
        });
    }

    private void configureTableColumns() {
        invNumCol.setCellValueFactory(data -> data.getValue().getInventoryNumberProperty());
        serialNumCol.setCellValueFactory(data -> data.getValue().getSerialNumberProperty());
        typeCol.setCellValueFactory(data -> data.getValue().getType().getNameProperty());
        nameCol.setCellValueFactory(data -> data.getValue().getNameProperty());
        locationCol.setCellValueFactory(data -> data.getValue().getLocationProperty());
        categoryCol.setCellValueFactory(data -> data.getValue().getCategoryProperty());
        isBundledCol.setCellValueFactory(cellData -> {
            boolean hasParent = cellData.getValue().getParent() != null;
            return new ReadOnlyStringWrapper(hasParent ? "Да" : "Нет");
        });
        fullCodeCol.setCellValueFactory(cellData -> cellData.getValue().getType().getFullCodeProperty());
    }

    private void setupEquipmentTable() {
        userEquipmentFiltered = new FilteredList<>(equipmentRepository.getEquipmentList());
        userEquipmentFiltered.setPredicate(e -> false);
        equipmentTable.setItems(userEquipmentFiltered);
    }

    private void loadUserAndEquipment() {
        String currentUsername = AppContext.getSessionManager().getUsername();
        if (currentUsername == null || currentUsername.isBlank()) return;
        Platform.runLater(() -> {
            UserDto user = userRepository.getUserList().stream()
                    .filter(u -> u.getUsername().equals(currentUsername))
                    .findFirst()
                    .orElse(null);
            if (user == null) {
                System.err.println("Пользователь не найден в репозитории: " + currentUsername);
                return;
            }
            currentUser = user;
//            currentUser = new UserDto();
//            currentUser.setId(user.getId());
//            currentUser.setUsername(user.getUsername());
//            currentUser.setName(user.getName());
//            currentUser.setSurname(user.getSurname());
//            currentUser.setPatronymic(user.getPatronymic());
//            currentUser.setMilitaryRank(user.getMilitaryRank());
//            currentUser.setCreatedAt(user.getCreatedAt());
//            currentUser.setPhoneNumber(user.getPhoneNumber());
//            currentUser.setRole(user.getRole());
//            currentUser.setStatus(user.getStatus());

            setUser(currentUser);
            updateEquipmentFilter();
        });
    }

    /**
     * Обновляет предикат фильтрации оборудования по текущему пользователю.
     */
    private void updateEquipmentFilter() {
        if (currentUser == null || currentUser.getId() == null) {
            userEquipmentFiltered.setPredicate(e -> false);
            return;
        }
        Long userId = currentUser.getId();
        userEquipmentFiltered.setPredicate(e -> e.getEmployee() != null &&
                e.getEmployee().getId().equals(userId));
    }

    private void setUser(UserDto user) {
        if (user == null) return;

        String fullName = String.format("%s %s %s",
                user.getSurname() != null ? user.getSurname() : "",
                user.getName() != null ? user.getName() : "",
                user.getPatronymic() != null ? user.getPatronymic() : "").trim();
        fullNameLabel.setText(fullName.isEmpty() ? "Не указано" : fullName);

        usernameLabel.setText(user.getUsername() != null ? user.getUsername() : "");
        rankLabel.setText(user.getMilitaryRank() != null ? user.getMilitaryRank() : "Не указано");
        phoneLabel.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "Не указан");

        UserRole role = user.getRole();
        roleLabel.setText(role != null ? getRoleDisplayName(role) : "");

        UserStatus status = user.getStatus();
        statusLabel.setText(status != null ? getStatusDisplayName(status) : "");

        if (user.getCreatedAt() != null) {
            createdAtLabel.setText(user.getCreatedAt().format(dateFormatter));
        } else {
            createdAtLabel.setText("");
        }
    }

    private String getRoleDisplayName(UserRole role) {
        if (role == null) return "";
        return switch (role) {
            case ADMIN -> "Администратор";
            case USER -> "Пользователь";
            case SUPERADMIN -> "Главный администратор";
            default -> role.name();
        };
    }

    private String getStatusDisplayName(UserStatus status) {
        if (status == null) return "";
        return switch (status) {
            case ACTIVE -> "Активен";
            case BLOCKED -> "Заблокирован";
            default -> status.name();
        };
    }
}