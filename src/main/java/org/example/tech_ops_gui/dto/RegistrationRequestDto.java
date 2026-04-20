package org.example.tech_ops_gui.dto;

public class RegistrationRequestDto {
    private String username;
    private String password;
    private String name;
    private String surname;
    private String patronymic;
    private String militaryRank;
    private String phoneNumber;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
