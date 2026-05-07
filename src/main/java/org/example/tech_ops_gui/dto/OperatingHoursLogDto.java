package org.example.tech_ops_gui.dto;

import java.time.LocalDateTime;

public class OperatingHoursLogDto {

    private Long id;
    private Integer hoursAdded;
    private LocalDateTime logDate;
    private UserDto user;
    private EquipmentDto equipment;

    public Long getId() {
        return id;
    }

    public Integer getHoursAdded() {
        return hoursAdded;
    }

    public LocalDateTime getLogDate() {
        return logDate;
    }

    public UserDto getUser() {
        return user;
    }

    public EquipmentDto getEquipment() {
        return equipment;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setHoursAdded(Integer hoursAdded) {
        this.hoursAdded = hoursAdded;
    }

    public void setLogDate(LocalDateTime logDate) {
        this.logDate = logDate;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public void setEquipment(EquipmentDto equipment) {
        this.equipment = equipment;
    }

    @Override
    public String toString() {
        return "OperatingHoursLogDto{" +
                "id=" + id +
                ", hoursAdded=" + hoursAdded +
                ", logDate=" + logDate +
                ", user=" + user +
                ", equipment=" + equipment +
                '}';
    }
}
