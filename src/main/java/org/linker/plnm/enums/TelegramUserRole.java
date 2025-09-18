package org.linker.plnm.enums;

public enum TelegramUserRole {
    ADMIN("administrator"),
    CREATOR("creator");

    private final String value;

    TelegramUserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isEqualTo(String role) {
        return value.equals(role);
    }
}
