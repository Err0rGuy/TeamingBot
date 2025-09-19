package org.linker.plnm.enums;

import lombok.Getter;

@Getter
public enum TelegramUserRole {
    ADMIN("administrator"),
    CREATOR("creator");

    private final String value;

    TelegramUserRole(String value) {
        this.value = value;
    }

    public boolean isEqualTo(String role) {
        return value.equals(role);
    }
}
