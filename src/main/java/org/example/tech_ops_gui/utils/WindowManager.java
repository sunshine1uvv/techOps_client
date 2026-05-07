package org.example.tech_ops_gui.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class WindowManager {

    private static final String FXML_PATH = "/org/example/tech_ops_gui/fxml/";

    /**
     * Универсальный метод для открытия модальных окон
     * @param fxmlName имя файла (например, "crud/equipment-edit-view.fxml")
     * @param title заголовок окна
     * @param controllerFactory лямбда для создания контроллера (если нужна передача данных)
     */
    public static void openModalWindow(String fxmlName, String title, Callback<Class<?>, Object> controllerFactory) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(FXML_PATH + fxmlName));

            if (controllerFactory != null) {
                loader.setControllerFactory(controllerFactory);
            }
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Ошибка загрузки FXML: " + fxmlName);
        }
    }

    /**
     * Открывает модальное окно, ждет его закрытия и возвращает его контроллер.
     * Идеально подходит для окон, где нужно получить ответ пользователя (например, настройки экспорта).
     */
    public static <T> T openModalAndWait(String fxmlName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(FXML_PATH + fxmlName));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Ошибка загрузки FXML для диалога: " + fxmlName);
            return null;
        }
    }

    public static void openModalWindow(String fxmlName, String title) {
        openModalWindow(fxmlName, title, null);
    }

    public static void close(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
