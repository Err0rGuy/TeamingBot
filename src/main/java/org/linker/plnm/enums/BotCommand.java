package org.linker.plnm.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BotCommand {
    START("/start", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT)),
    HINT("/hint", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.CALLBACK)),
    SHOW_TEAMS("/show_teams", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT)),
    EDIT_TEAM_MENU("/edit_team", List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    CREATE_TEAM("/create_team", List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    REMOVE_TEAM("/remove_team", List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    ADD_MEMBER("/add_member", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    RENAME_TEAM("/rename_team", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    REMOVE_MEMBER("/remove_member", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK));

    public enum CommandType {
        PRIVILEGED,
        UNPRIVILEGED,
        CALLBACK,
        TEXT
    }

    private final String command;

    private List<CommandType> types;

    BotCommand(String command) {
        this.command  = command;
    }

    BotCommand(String command, List<CommandType> types){
        this.command = command;
        this.types = types;
    }

    public String str() {
        return command;
    }

    public boolean isEqualTo(String command) {
        return command.equals(this.command);
    }

    public boolean isCallback() {
        return types.contains(CommandType.CALLBACK);
    }

    public boolean isText() {
        return types.contains(CommandType.TEXT);
    }

    public boolean isPrivileged() {
        return types.contains(CommandType.PRIVILEGED);
    }

    public static BotCommand getCommand(String command) {
        return Arrays.stream(values())
                .filter(c -> command.equals(c.str())).findFirst().orElse(null);
    }

    private static List<BotCommand> filterType(CommandType type) {
        return Arrays.stream(values())
            .filter(botCommands -> botCommands.types.contains(type))
            .collect(Collectors.toList());
    }

    public static boolean commandTypeIs(CommandType type, String command) {
        return BotCommand.filterType(type).stream()
                .map(BotCommand::str).toList().contains(command);
    }

    public static boolean isPrivileged(String command) {
        return BotCommand.filterType(CommandType.PRIVILEGED).stream()
                .map(BotCommand::str).toList().contains(command);
    }

    public static boolean isUnPrivileged(String command) {
        return BotCommand.filterType(CommandType.UNPRIVILEGED).stream()
                .map(BotCommand::str).toList().contains(command);
    }

    public static boolean isCallback(String command) {
        return BotCommand.filterType(CommandType.CALLBACK).stream()
                .map(BotCommand::str).toList().contains(command);
    }

    public static boolean isText(String command) {
        return BotCommand.filterType(CommandType.TEXT).stream()
                .map(BotCommand::str).toList().contains(command);
    }
}
