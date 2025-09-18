package org.linker.plnm.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BotCommands {
    START("/start", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT)),
    HINT("/hint", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.CALLBACK)),
    CREATE_TEAM("/create_team", List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    REMOVE_TEAM("/remove_team",  List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    EDIT_TEAM("/edit_team",  List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    SHOW_TEAMS("/show_teams",   List.of(CommandType.UNPRIVILEGED, CommandType.TEXT)),
    RENAME_TEAM("/rename_team",   List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    ADD_MEMBER("/add_member",   List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    REMOVE_MEMBER("/remove_member", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),;

    public enum CommandType {
        PRIVILEGED,
        UNPRIVILEGED,
        CALLBACK,
        TEXT
    }

    private final String command;

    private List<CommandType> types;

    BotCommands(String command) {
        this.command  = command;
    }

    BotCommands(String command, List<CommandType> types){
        this.command = command;
        this.types = types;
    }

    public String str() {
        return command;
    }

    public boolean isEqualTo(String command){
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

    public static BotCommands getCommand(String command) {
        return Arrays.stream(values())
                .filter(c -> command.equals(c.str())).findFirst().orElse(null);
    }

    private static List<BotCommands> filterType(CommandType type) {
        return Arrays.stream(values())
            .filter(botCommands -> botCommands.types.contains(type))
            .collect(Collectors.toList());
    }

    public static boolean commandTypeIs(CommandType type, String command) {
        return BotCommands.filterType(type).stream()
                .map(BotCommands::str).toList().contains(command);
    }

    public static boolean isPrivileged(String command) {
        return BotCommands.filterType(CommandType.PRIVILEGED).stream()
                .map(BotCommands::str).toList().contains(command);
    }

    public static boolean isUnPrivileged(String command) {
        return BotCommands.filterType(CommandType.UNPRIVILEGED).stream()
                .map(BotCommands::str).toList().contains(command);
    }

    public static boolean isCallback(String command) {
        return BotCommands.filterType(CommandType.CALLBACK).stream()
                .map(BotCommands::str).toList().contains(command);
    }

    public static boolean isText(String command) {
        return BotCommands.filterType(CommandType.TEXT).stream()
                .map(BotCommands::str).toList().contains(command);
    }



}
