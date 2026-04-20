package org.example.tech_ops_gui.entities;

import java.time.LocalDateTime;


public class User {

    private Long id;

    private String username;

    private String passwordHash;

    private String name;

    private String surname;

    private String patronymic;

    private String militaryRank;

    private String phoneNumber;

    private UserRole role = UserRole.USER;

    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserStatus getStatus() {
        return status;
    }

    public UserRole getRole() {
        return role;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMilitaryRank() {
        return militaryRank;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public void setMilitaryRank(String militaryRank) {
        this.militaryRank = militaryRank;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

