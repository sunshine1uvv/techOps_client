package org.example.tech_ops_gui.enums;

public enum MilitaryRank {
    // Солдаты и сержанты
    PRIVATE("Рядовой"),
    CORPORAL("Ефрейтор"),
    JUNIOR_SERGEANT("Младший сержант"),
    SERGEANT("Сержант"),
    SENIOR_SERGEANT("Старший сержант"),
    STARSHINA("Старшина"),

    // Прапорщики
    WARRANT_OFFICER("Прапорщик"),
    SENIOR_WARRANT_OFFICER("Старший прапорщик"),

    // Младшие офицеры
    JUNIOR_LIEUTENANT("Младший лейтенант"),
    LIEUTENANT("Лейтенант"),
    SENIOR_LIEUTENANT("Старший лейтенант"),
    CAPTAIN("Капитан"),

    // Старшие офицеры
    MAJOR("Майор"),
    LIEUTENANT_COLONEL("Подполковник"),
    COLONEL("Полковник"),

    // Высшие офицеры
    MAJOR_GENERAL("Генерал-майор"),
    LIEUTENANT_GENERAL("Генерал-лейтенант"),
    COLONEL_GENERAL("Генерал-полковник"),

    // Гражданский персонал
    CIVILIAN("Служащий (Гражданский персонал)");

    private final String displayName;

    MilitaryRank(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}