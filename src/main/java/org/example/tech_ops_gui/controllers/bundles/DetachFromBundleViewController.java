package org.example.tech_ops_gui.controllers.bundles;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.example.tech_ops_gui.config.AppContext;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.exceptions.CustomExceptionHandler;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.NotificationManager;
import org.example.tech_ops_gui.utils.WindowManager;

public class DetachFromBundleViewController {

    private final EquipmentDto selectedItem;
    private final EquipmentRepository equipmentRepository = AppContext.getEquipmentRepository();


    public DetachFromBundleViewController(EquipmentDto selectedItem) {
        this.selectedItem = selectedItem;
    }

    @FXML
    private void handleRemoveClick(ActionEvent event) {
        equipmentRepository.detach(selectedItem.getId())
                .thenRun(() -> Platform.runLater(() -> {
                    NotificationManager.showInfo("Успех", "Оборудование успешно выведено из комплекта.");
                    WindowManager.close(event);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> CustomExceptionHandler.handleError(ex));
                    return null;
                });
    }

    @FXML
    private void handleCloseClick(ActionEvent event) {
        WindowManager.close(event);
    }

}
