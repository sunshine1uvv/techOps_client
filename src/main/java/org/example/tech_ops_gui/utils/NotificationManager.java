package org.example.tech_ops_gui.utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class NotificationManager {

    /**
     * Показать информационное сообщение (успех)
     */
    public static void showInfo(String title, String message) {
        Platform.runLater(() -> Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.BOTTOM_RIGHT)
                .hideAfter(Duration.seconds(4))
                .showInformation());
    }

    /**
     * Показать предупреждение
     */
    public static void showWarning(String title, String message) {
        Platform.runLater(() -> Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.BOTTOM_RIGHT)
                .hideAfter(Duration.seconds(5))
                .showWarning());
    }

    /**
     * Показать ошибку
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> Notifications.create()
                .title(title)
                .text(message)
                .position(Pos.BOTTOM_RIGHT)
                .hideAfter(Duration.seconds(5))
                .showError());
    }
}
