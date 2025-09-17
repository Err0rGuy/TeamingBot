package org.linker.plnm.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BotCommands {
    START("/start"),
    HINT("/hint"),
    CREATE_TEAM("/create_team"),
    REMOVE_TEAM("/remove_team"),
    EDIT_TEAM("/edit_team"),
    SHOW_TEAMS("/show_teams"),
    RENAME_TEAM("/rename_team"),
    ADD_MEMBER("/add_member"),
    REMOVE_MEMBER("/remove_member");

    public enum CommandType {
        PRIVILEGED,
        UNPRIVILEGED,
        CALLBACK,
        TEXT
    }

    private String command;

    private List<CommandType> types;

    BotCommands(String command) {
        this.command  = command;
    }

    BotCommands(String command, List<CommandType> types){
        this.command = command;
        this.types = types;
    }

    public String getCmd() {
        return command;
    }

    private static List<BotCommands> filterType(CommandType type) {
        return Arrays.stream(values())
            .filter(botCommands -> botCommands.types.contains(type))
            .collect(Collectors.toList());
    }

    public static boolean commandTypeIs(CommandType type, String command) {
        return BotCommands.filterType(type).stream()
                .map(BotCommands::getCmd).toList().contains(command);
    }

    public static boolean isPrivileged(String command) {
        return BotCommands.filterType(CommandType.PRIVILEGED).stream()
                .map(BotCommands::getCmd).toList().contains(command);
    }

    public static boolean isUnPrivileged(String command) {
        return BotCommands.filterType(CommandType.UNPRIVILEGED).stream()
                .map(BotCommands::getCmd).toList().contains(command);
    }

    public static boolean isCallback(String command) {
        return BotCommands.filterType(CommandType.CALLBACK).stream()
                .map(BotCommands::getCmd).toList().contains(command);
    }

    public static boolean isText(String command) {
        return BotCommands.filterType(CommandType.TEXT).stream()
                .map(BotCommands::getCmd).toList().contains(command);
    }
}
