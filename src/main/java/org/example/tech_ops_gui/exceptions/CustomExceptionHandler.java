package org.example.tech_ops_gui.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.example.tech_ops_gui.utils.NotificationManager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletionException;

public class CustomExceptionHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void handleError(Throwable ex) {
        Platform.runLater(() -> {
            String title = "Ошибка системы";
            String message = "Не удалось связаться с сервером";
            boolean isWarning = false;

            try {
                Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                String responseBody = extractBody(cause);

                if (responseBody != null && responseBody.trim().startsWith("{")) {
                    JsonNode rootNode = mapper.readTree(responseBody);
                    isWarning = true;

                    if (rootNode.has("fieldErrors") && !rootNode.get("fieldErrors").isNull() && !rootNode.get("fieldErrors").isEmpty()) {
                        title = "Ошибка заполнения полей";
                        StringBuilder sb = new StringBuilder();
                        JsonNode fieldErrors = rootNode.get("fieldErrors");
                        Iterator<Map.Entry<String, JsonNode>> fields = fieldErrors.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> field = fields.next();
                            sb.append("• ").append(field.getValue().asText()).append("\n");
                        }
                        message = sb.toString().trim();

                    } else if (rootNode.has("message")) {
                        title = "Внимание";
                        message = rootNode.get("message").asText();
                    }
                } else {
                    isWarning = false;
                    title = "Сетевая ошибка";
                    String rawMsg = cause.getMessage();
                    message = (rawMsg != null && rawMsg.contains("Connection refused"))
                            ? "Сервер недоступен. Проверьте соединение." : rawMsg;
                }
            } catch (Exception e) {
                title = "Критический сбой";
                message = "Ошибка разбора ответа: " + e.getMessage();
                isWarning = false;
            }

            if (isWarning) {
                NotificationManager.showWarning(title, message);
            } else {
                NotificationManager.showError(title, message);
            }
        });
    }

    private static String extractBody(Throwable cause) {
        String msg = cause.getMessage();
        if (msg != null && msg.contains("{")) {
            return msg.substring(msg.indexOf("{"));
        }
        return msg;
    }
}
