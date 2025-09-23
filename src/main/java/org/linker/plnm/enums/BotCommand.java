package org.linker.plnm.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BotCommand {
    START("/start", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.PRIVATE_CHAT_ALLOWED)),
    COMMANDS("/commands", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.CALLBACK, CommandType.PRIVATE_CHAT_ALLOWED)),
    SHOW_TEAMS("/show_teams", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT)),
    MY_TEAMS("/my_teams", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT)),
    EDIT_TEAM_MENU("/edit_team", List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    CREATE_TEAM("/create_team", List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    RENAME_TEAM("/rename_team", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAM_EDITING)),
    REMOVE_TEAM("/remove_team", List.of(CommandType.PRIVILEGED, CommandType.TEXT)),
    ADD_MEMBER("/add_member", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_EDITING)),
    REMOVE_MEMBER("/remove_member", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_EDITING)),
    TASKS_MENU("/tasks_menu", List.of(CommandType.PRIVILEGED, CommandType.TEXT, CommandType.CALLBACK)),
    /// Returns tasks menu in a new message
    TASKS_MENU_NEW("/tasks_menu_new", List.of(CommandType.PRIVILEGED, CommandType.TEXT, CommandType.CALLBACK)),
    CREATE_TASK_MENU("menu:createTask", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    REMOVE_TASK_MENU("menu:removeTask", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    CH_TASK_STATUS_MENU("menu:chTaskStatus", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    CREATE_MEMBER_TASK("/create_member_task", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_TASK_ACTION)),
    CREATE_TEAM_TASK("/create_team_task", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAM_TASK_ACTION)),
    REMOVE_MEMBER_TASK("/remove_member_task", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_TASK_ACTION)),
    REMOVE_TEAM_TASK("/remove_team_task", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAM_TASK_ACTION)),
    CH_MEMBER_TASK_STATUS("/ch_member_task_status", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_TASK_ACTION)),
    CH_TEAM_TASK_STATUS("/ch_team_task_status", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAM_TASK_ACTION)),
    SEE_TASKS_MENU("menu:seeTask", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    SEE_TEAM_TASKS("/see_team_tasks", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAM_TASK_ACTION)),
    SEE_MEMBER_TASKS("/see_member_tasks", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_TASK_ACTION));

    public enum CommandType {
        PRIVATE_CHAT_ALLOWED,
        PRIVILEGED,
        UNPRIVILEGED,
        CALLBACK,
        TEXT,
        TEAM_EDITING,
        MEMBER_EDITING,
        TEAM_TASK_ACTION,
        MEMBER_TASK_ACTION
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

    public boolean isTeamEditing() {
        return types.contains(CommandType.TEAM_EDITING);
    }

    public boolean isMemberEditing() {
        return types.contains(CommandType.MEMBER_EDITING);
    }

    public boolean isTeamTaskAction() {
        return types.contains(CommandType.TEAM_TASK_ACTION);
    }

    public boolean isMemberTaskAction() {
        return types.contains(CommandType.MEMBER_TASK_ACTION);
    }

    public boolean isText() {
        return types.contains(CommandType.TEXT);
    }

    public boolean isPrivileged() {
        return types.contains(CommandType.PRIVILEGED);
    }

    public boolean isPrivateChatAllowed() {
        return types.contains(CommandType.PRIVATE_CHAT_ALLOWED);
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
