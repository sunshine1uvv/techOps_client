package org.example.tech_ops_gui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.tech_ops_gui.synchronization.WebSocketSyncClient;

public class MainApplication extends Application {

    @Override
    public void init() {
        try {
            WebSocketSyncClient.getInstance().connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/tech_ops_gui/fxml/auth/login-view.fxml"));
        primaryStage.setTitle("Авторизация");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        WebSocketSyncClient.getInstance().shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}