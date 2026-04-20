package org.example.tech_ops_gui.entities;

import javafx.beans.property.*;

public class EquipmentType {
    private SimpleLongProperty id= new SimpleLongProperty();
    private ObjectProperty<EquipmentType> parent = new SimpleObjectProperty<EquipmentType>();
    private SimpleStringProperty name= new SimpleStringProperty();
    private SimpleIntegerProperty level= new SimpleIntegerProperty();
    private SimpleStringProperty code= new SimpleStringProperty();
    private SimpleStringProperty fullCode = new SimpleStringProperty();

    public SimpleLongProperty getIdProperty() {
        return this.id;
    }
    public ObjectProperty<EquipmentType> getParentProperty() {
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
    public void setParent(EquipmentType parent) {
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
    public EquipmentType getParent() {
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
}

