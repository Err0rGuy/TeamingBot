package org.linker.plnm.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum BotActions {
    START("/start", "See description"),
    HINT("/hint", "See commands"),
    CREATE_TEAM("/create_team", "Create a new team"),
    REMOVE_TEAM("/remove_team", "Remove a team"),
    EDIT_TEAM("/edit_team", "Edit a team"),
    RENAME_TEAM("/rename_team", "Rename a team"),
    ADD_MEMBER("/add_member", "Add a member"),
    REMOVE_MEMBER("/remove_member", "Remove a member");

    private final String command;
    private final String description;

    BotActions(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public static Optional<BotActions> fromText(String text) {
        return Arrays.stream(values())
                .filter(a -> a.command.equalsIgnoreCase(text))
                .findFirst();
    }}
