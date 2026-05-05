package org.example.tech_ops_gui.dto;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.example.tech_ops_gui.enums.UserRole;
import org.example.tech_ops_gui.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserDto {

    private final SimpleLongProperty id = new SimpleLongProperty();
    private final SimpleStringProperty username = new SimpleStringProperty();
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty surname = new SimpleStringProperty();
    private final SimpleStringProperty patronymic = new SimpleStringProperty();
    private final SimpleStringProperty militaryRank = new SimpleStringProperty();
    private final SimpleStringProperty phoneNumber = new SimpleStringProperty();
    private final ObjectProperty<UserRole> role = new SimpleObjectProperty<>(UserRole.USER);
    private final ObjectProperty<UserStatus> status = new SimpleObjectProperty<>(UserStatus.ACTIVE);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    // ---------- Геттеры свойств ----------
    public SimpleLongProperty getIdProperty() {
        return id;
    }

    public SimpleStringProperty getUsernameProperty() {
        return username;
    }

    public SimpleStringProperty getNameProperty() {
        return name;
    }

    public SimpleStringProperty getSurnameProperty() {
        return surname;
    }

    public SimpleStringProperty getPatronymicProperty() {
        return patronymic;
    }

    public SimpleStringProperty getMilitaryRankProperty() {
        return militaryRank;
    }

    public SimpleStringProperty getPhoneNumberProperty() {
        return phoneNumber;
    }

    public ObjectProperty<UserRole> getRoleProperty() {
        return role;
    }

    public ObjectProperty<UserStatus> getStatusProperty() {
        return status;
    }

    public ObjectProperty<LocalDateTime> getCreatedAtProperty() {
        return createdAt;
    }

    public Long getId() {
        return id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getSurname() {
        return surname.get();
    }

    public void setSurname(String surname) {
        this.surname.set(surname);
    }

    public String getPatronymic() {
        return patronymic.get();
    }

    public void setPatronymic(String patronymic) {
        this.patronymic.set(patronymic);
    }

    public String getMilitaryRank() {
        return militaryRank.get();
    }

    public void setMilitaryRank(String militaryRank) {
        this.militaryRank.set(militaryRank);
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }

    public UserRole getRole() {
        return role.get();
    }

    public void setRole(UserRole role) {
        this.role.set(role);
    }

    public UserStatus getStatus() {
        return status.get();
    }

    public void setStatus(UserStatus status) {
        this.status.set(status);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(getId(), userDto.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", getSurname(), getName(), getPatronymic()).trim();
    }
}