package org.example.tech_ops_gui.utils;

import org.example.tech_ops_gui.dto.EquipmentDto;
import java.util.function.Predicate;

public class EquipmentHierarchyUtil {

    // Проверка: является ли оборудование свободным (не в комплекте) и не равно ли оно самому себе
    public static boolean isRootEquipment(EquipmentDto equipment, EquipmentDto excludedItem) {
        return equipment.getParent() == null &&
                equipment.getInventoryNumber() != null &&
                !equipment.getId().equals(excludedItem.getId());
    }

    // Проверка: относится ли измененное оборудование к нашему комплекту (для WebSocket)
    public static boolean isRelevantForBundle(EquipmentDto equipment, Long rootId) {
        if (equipment == null) return false;
        if (equipment.getId().equals(rootId)) return true; // Это сам корень
        return equipment.getParent() != null && equipment.getParent().getId().equals(rootId); // Это прямой потомок
    }

    // Сборка сложного фильтра для окна добавления в комплект
    public static Predicate<EquipmentDto> buildAddInBundlePredicate(EquipmentDto selectedItem, String filterText) {
        return equipment -> {
            if (!isRootEquipment(equipment, selectedItem)) return false;
            if (filterText == null || filterText.isBlank()) return true;

            String f = filterText.toLowerCase();
            return (equipment.getName() != null && equipment.getName().toLowerCase().contains(f)) ||
                    (equipment.getSerialNumber() != null && equipment.getSerialNumber().toLowerCase().contains(f)) ||
                    (equipment.getInventoryNumber() != null && equipment.getInventoryNumber().toLowerCase().contains(f)) ||
                    (equipment.getType() != null && equipment.getType().getName().toLowerCase().contains(f));
        };
    }
}