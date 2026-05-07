package org.example.tech_ops_gui.services;

import org.example.tech_ops_gui.dto.EquipmentDto;
import org.example.tech_ops_gui.repository.EquipmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EquipmentBatchService {

    private final EquipmentRepository repository;

    public EquipmentBatchService(EquipmentRepository repository) {
        this.repository = repository;
    }

    // --- Валидация перед генерацией ---
    public String validateMassAddParams(String invStart, int invStep, String serStart, int quantity) {
        if (invStart != null) {
            String upperInv = invStart.toUpperCase();
            if (!upperInv.matches("^ИТ\\d{5}$")) return "Инвентарный номер должен быть в формате 'ИТ' и ровно 5 цифр.";
            int startNum = Integer.parseInt(upperInv.substring(2));
            if (startNum + invStep * (quantity - 1) > 99999) return "Серия выходит за пределы ИТ99999. Уменьшите количество или шаг.";
        }
        if (serStart != null && !serStart.matches(".*\\d.*")) {
            return "Серийный номер должен содержать хотя бы одну цифру.";
        }
        return null; // Ошибок нет
    }

    // --- Генерация партии ---
    public List<EquipmentDto> generateBatch(EquipmentDto baseDto, String startInv, int invStep, String startSerial, int serialStep, int count) {
        List<EquipmentDto> batch = new ArrayList<>();
        String currInv = startInv != null ? startInv.toUpperCase() : null;
        String currSer = startSerial;

        for (int i = 0; i < count; i++) {
            EquipmentDto dto = cloneBaseDto(baseDto);
            dto.setInventoryNumber(currInv);
            dto.setSerialNumber(currSer);
            batch.add(dto);

            if (currInv != null) currInv = incrementInventoryNumber(currInv, invStep);
            if (currSer != null) currSer = incrementSerialSuffix(currSer, serialStep);
        }
        return batch;
    }

    public String checkConflict(String inv, String ser, List<EquipmentDto> currentTableItems) {
        Set<String> dbInvs = repository.findAllInventoryNumbers();
        Set<String> dbSerials = repository.findAllSerialNumbers();

        boolean internalInvDup = currentTableItems.stream().anyMatch(w -> Objects.equals(w.getInventoryNumber(), inv));
        boolean internalSerDup = currentTableItems.stream().anyMatch(w -> Objects.equals(w.getSerialNumber(), ser));

        StringBuilder sb = new StringBuilder();
        if (inv != null && (dbInvs.contains(inv) || internalInvDup)) sb.append("Инв. номер занят; ");
        if (ser != null && (dbSerials.contains(ser) || internalSerDup)) sb.append("Сер. номер занят; ");

        return sb.length() == 0 ? "Исправлено" : sb.toString().replaceAll("; $", "");
    }

    public boolean hasDatabaseConflict(EquipmentDto item) {
        Set<String> dbInvs = repository.findAllInventoryNumbers();
        Set<String> dbSerials = repository.findAllSerialNumbers();
        return (item.getInventoryNumber() != null && dbInvs.contains(item.getInventoryNumber())) ||
                (item.getSerialNumber() != null && dbSerials.contains(item.getSerialNumber()));
    }

    // --- Внутренние методы генерации ---
    private EquipmentDto cloneBaseDto(EquipmentDto base) {
        EquipmentDto dto = new EquipmentDto();
        dto.setType(base.getType());
        dto.setName(base.getName());
        dto.setLocation(base.getLocation());
        dto.setEmployee(base.getEmployee());
        dto.setCategory(base.getCategory());
        dto.setDepartment(base.getDepartment());
        dto.setMaxOperatingHours(base.getMaxOperatingHours());
        return dto;
    }

    private String incrementInventoryNumber(String invNum, int step) {
        if (invNum == null || !invNum.matches("^ИТ\\d{5}$")) return invNum;
        int num = Integer.parseInt(invNum.substring(2));
        return String.format("ИТ%05d", num + step);
    }

    private String incrementSerialSuffix(String base, int step) {
        if (base == null || base.isEmpty()) return base;
        Matcher m = Pattern.compile("^(.*?)(\\d+)(\\D*)$").matcher(base);
        if (m.matches()) return m.group(1) + String.format("%0" + m.group(2).length() + "d", Integer.parseInt(m.group(2)) + step) + m.group(3);
        return base;
    }
}