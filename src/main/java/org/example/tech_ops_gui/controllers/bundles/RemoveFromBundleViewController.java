package org.example.tech_ops_gui.controllers.bundles;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.repository.EquipmentRepository;
import org.example.tech_ops_gui.utils.WindowManager;

public class RemoveFromBundleViewController {

    private final EquipmentDto selectedItem;
    private final EquipmentRepository equipmentRepository = EquipmentRepository.getInstance();


    public RemoveFromBundleViewController(EquipmentDto selectedItem) {
        this.selectedItem = selectedItem;
    }

    @FXML
    private void handleRemoveClick(ActionEvent event) {
        equipmentRepository.detach(selectedItem.getId());
    }

    @FXML
    private void handleCloseClick(ActionEvent event) {
        WindowManager.close(event);
    }

}
