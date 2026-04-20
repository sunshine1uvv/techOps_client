package org.example.tech_ops_gui.entities;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;


public class Equipment {
    private ObjectProperty<Long> id= new SimpleObjectProperty<>();
    private ObjectProperty<Equipment> parent= new SimpleObjectProperty<Equipment>();
    private ObjectProperty<EquipmentType> type = new SimpleObjectProperty<EquipmentType>();
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty inventoryNumber = new SimpleStringProperty();
    private SimpleStringProperty serialNumber= new SimpleStringProperty();
    private SimpleStringProperty employee= new SimpleStringProperty();
    private SimpleStringProperty location= new SimpleStringProperty();
    private ObjectProperty<Integer> category = new SimpleObjectProperty<>();

    public ObjectProperty<Long> getIdProperty() {
        return this.id;
    }
    public ObjectProperty<Equipment> getParentProperty() {
        return this.parent;
    }
    public ObjectProperty<EquipmentType> getTypeProperty() {
        return this.type;
    }
    public SimpleStringProperty getNameProperty() {
        return this.name;
    }
    public SimpleStringProperty getInventoryNumberProperty() {
        return this.inventoryNumber;
    }
    public SimpleStringProperty getSerialNumberProperty() {
        return this.serialNumber;
    }
    public SimpleStringProperty getEmployeeProperty() {
        return this.employee;
    }
    public SimpleStringProperty getLocationProperty() {
        return this.location;
    }
    public ObjectProperty<Integer> getCategoryProperty() {
        return this.category;
    }



    public void setId(Long id) {
        this.id.set(id);
    }
    public void setParent(Equipment parent) {
        this.parent.set(parent);
    }
    public void setType(EquipmentType type) {
        this.type.set(type);
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public void setInventoryNumber(String inventoryNumber) {
        this.inventoryNumber.set(inventoryNumber);
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber.set(serialNumber);
    }
    public void setEmployee(String employee) {
        this.employee.set(employee);
    }
    public void setLocation(String location) {
        this.location.set(location);
    }
    public void setCategory(Integer category) {
        this.category.set(category);
    }

    public Long getId() {
        return this.id.get();
    }
    public Equipment getParent() {
        return this.parent.get();
    }
    public EquipmentType getType() {
        return this.type.get();
    }
    public String getName() {
        return this.name.get();
    }
    public String getInventoryNumber(){
        return this.inventoryNumber.get();
    }
    public String getSerialNumber(){
        return this.serialNumber.get();
    }
    public String getEmployee() {
        return this.employee.get();
    }
    public String getLocation() {
        return this.location.get();
    }
    public Integer getCategory() {
        return this.category.get();
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "id=" + id +
                ", parent=" + parent +
                ", type=" + type +
                ", name=" + name +
                ", inventoryNumber=" + inventoryNumber +
                ", serialNumber=" + serialNumber +
                ", employee=" + employee +
                ", location=" + location +
                ", category=" + category +
                '}';
    }
}

