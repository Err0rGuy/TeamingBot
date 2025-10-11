package org.linker.plnm.enums;

public enum MessageParseMode {
    HTML("HTML"),
    MARK_DOWN("Markdown");

    private final String parseMode;

    MessageParseMode(String parseMode) {
        this.parseMode = parseMode;
    }

    @Override
    public String toString() {
        return parseMode;
    }
}
