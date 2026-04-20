package org.example.tech_ops_gui.dto;

import java.time.LocalDateTime;

public class RequestResponseDto {
    private Long id;
    private String username;
    private String name;
    private String surname;
    private String patronymic;
    private String militaryRank;
    private String phoneNumber;
    private String requestedRole;
    private String status;
    private LocalDateTime createdAt;
    private String reviewedByUsername;
    private LocalDateTime reviewedAt;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public String getMilitaryRank() {
        return militaryRank;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRequestedRole() {
        return requestedRole;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getReviewedByUsername() {
        return reviewedByUsername;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public void setRequestedRole(String requestedRole) {
        this.requestedRole = requestedRole;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setReviewedByUsername(String reviewedByUsername) {
        this.reviewedByUsername = reviewedByUsername;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

}