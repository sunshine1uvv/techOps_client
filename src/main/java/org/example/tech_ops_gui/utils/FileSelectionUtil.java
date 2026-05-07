package org.example.tech_ops_gui.utils;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;

public class FileSelectionUtil {

    /**
     * Открывает диалог выбора существующего файла (Open Dialog)
     *
     * @param ownerWindow Родительское окно (чтобы диалог не терялся на фоне)
     * @param title Заголовок окна диалога
     * @param filterName Название фильтра (например, "Excel Files")
     * @param extensions Допустимые расширения (например, "*.xlsx", "*.xls")
     * @return Выбранный файл File или null, если пользователь нажал "Отмена"
     */
    public static File chooseFileForOpen(Window ownerWindow, String title, String filterName, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, extensions));
        return fileChooser.showOpenDialog(ownerWindow);
    }

    /**
     * Открывает диалог сохранения нового файла (Save Dialog)
     *
     * @param ownerWindow Родительское окно
     * @param title Заголовок окна диалога
     * @param initialFileName Имя файла по умолчанию
     * @param filterName Название фильтра
     * @param extensions Допустимые расширения
     * @return Выбранный путь для сохранения File или null
     */
    public static File chooseFileForSave(Window ownerWindow, String title, String initialFileName, String filterName, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        if (initialFileName != null && !initialFileName.isBlank()) {
            fileChooser.setInitialFileName(initialFileName);
        }

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, extensions));
        return fileChooser.showSaveDialog(ownerWindow);
    }
}