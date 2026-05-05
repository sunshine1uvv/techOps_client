module org.example.tech_ops_gui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires spring.websocket;
    requires spring.messaging;
    requires java.net.http;
    requires spring.web;
    requires com.google.gson;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires java.naming;
    requires spring.context;
    requires org.yaml.snakeyaml;

    opens org.example.tech_ops_gui to javafx.fxml;
    opens org.example.tech_ops_gui.controllers to javafx.fxml;
    opens org.example.tech_ops_gui.controllers.crud to javafx.fxml;
    opens org.example.tech_ops_gui.controllers.auth to javafx.fxml;
    opens org.example.tech_ops_gui.controllers.bundles to javafx.fxml;
    opens org.example.tech_ops_gui.synchronization;
    exports org.example.tech_ops_gui;
    opens org.example.tech_ops_gui.controllers.admin to javafx.fxml;
    opens org.example.tech_ops_gui.dto to com.fasterxml.jackson.databind, javafx.base;
    opens org.example.tech_ops_gui.enums to com.fasterxml.jackson.databind;
}