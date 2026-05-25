package org.example.tech_ops_gui.dto;

import javafx.beans.property.*;

import java.util.Objects;

public class EquipmentTypeDto {
    private ObjectProperty<Long> id = new SimpleObjectProperty<>();
    private ObjectProperty<EquipmentTypeDto> parent = new SimpleObjectProperty<EquipmentTypeDto>();
    private SimpleStringProperty name= new SimpleStringProperty();
    private SimpleIntegerProperty level= new SimpleIntegerProperty();
    private SimpleStringProperty code= new SimpleStringProperty();
    private SimpleStringProperty fullCode = new SimpleStringProperty();

    public  ObjectProperty<Long> getIdProperty() {
        return this.id;
    }
    public ObjectProperty<EquipmentTypeDto> getParentProperty() {
        return this.parent;
    }
    public SimpleStringProperty getNameProperty() {
        return this.name;
    }
    public SimpleIntegerProperty getLevelProperty() {
        return this.level;
    }
    public SimpleStringProperty getCodeProperty() {
        return this.code;
    }
    public SimpleStringProperty getFullCodeProperty() {
        return this.fullCode;
    }

    public void setId(Long id) {
        this.id.set(id);
    }
    public void setParent(EquipmentTypeDto parent) {
        this.parent.set(parent);
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public void setLevel(Integer level) {
        this.level.set(level);
    }
    public void setCode(String code) {
        this.code.set(code);
    }
    public void setFullCode(String fullCode) {
        this.fullCode.set(fullCode);
    }

    public Long getId() {
        return this.id.get();
    }
    public EquipmentTypeDto getParent() {
        return this.parent.get();
    }
    public String getName() {
        return this.name.get();
    }
    public Integer getLevel() {
        return this.level.get();
    }
    public String getCode() {
        return this.code.get();
    }
    public String getFullCode() {
        return this.fullCode.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquipmentTypeDto that = (EquipmentTypeDto) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

