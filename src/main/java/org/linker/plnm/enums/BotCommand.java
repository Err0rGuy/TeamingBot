package org.linker.plnm.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BotCommand {
    START("/start", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.PRIVATE_CHAT_ALLOWED)),
    COMMANDS("/commands", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.CALLBACK, CommandType.PRIVATE_CHAT_ALLOWED)),
    SHOW_TEAMS("/show_teams", List.of(CommandType.UNPRIVILEGED, CommandType.CALLBACK, CommandType.TEAMING_ACTION)),
    MY_TEAMS("/my_teams", List.of(CommandType.UNPRIVILEGED, CommandType.CALLBACK, CommandType.TEAMING_ACTION)),
    EDIT_TEAM_MENU("/edit_team", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAMING_ACTION)),
    CREATE_TEAM("/create_team", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAMING_ACTION)),
    RENAME_TEAM("/rename_team", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAMING_ACTION)),
    REMOVE_TEAM("/remove_team", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAMING_ACTION)),
    ADD_MEMBER("/add_member", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAMING_ACTION)),
    REMOVE_MEMBER("/remove_member", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAMING_ACTION)),
    TASKS_MENU("/tasks_menu", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.CALLBACK)),
    TASKS_MENU_BACK("/tasks_menu_back", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.CALLBACK)),
    TEAMS_MENU("/teams_menu", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.CALLBACK)),
    TEAMS_MENU_BACK("/teams_menu_back", List.of(CommandType.UNPRIVILEGED, CommandType.TEXT, CommandType.CALLBACK)),
    /// Returns tasks menu in a new message
    CREATE_TASK_MENU("menu:createTask", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    REMOVE_TASK_MENU("menu:removeTask", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    CREATE_MEMBER_TASK("/create_member_task", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_TASK_ACTION)),
    CREATE_TEAM_TASK("/create_team_task", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAM_TASK_ACTION)),
    REMOVE_MEMBER_TASK("/remove_member_task", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_TASK_ACTION)),
    REMOVE_TEAM_TASK("/remove_team_task", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAM_TASK_ACTION)),
    UPDATE_TASK_STATUS("/update_task_status", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    SHOW_TASKS_MENU("menu:seeTask", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK)),
    SHOW_TEAM_TASKS("/see_team_tasks", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.TEAM_TASK_ACTION)),
    SHOW_MEMBER_TASKS("/see_member_tasks", List.of(CommandType.PRIVILEGED, CommandType.CALLBACK, CommandType.MEMBER_TASK_ACTION));

    public enum CommandType {
        PRIVATE_CHAT_ALLOWED,
        PRIVILEGED,
        UNPRIVILEGED,
        CALLBACK,
        TEXT,
        TEAMING_ACTION,
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

    public boolean isTeamingAction() {
        return types.contains(CommandType.TEAMING_ACTION);
    }

    public boolean isTaskingAction() {
        return isTeamTaskAction() || isMemberTaskAction();
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

    public boolean isTaskCreation() {
        return command.equals(BotCommand.CREATE_TEAM_TASK.str())
                || command.equalsIgnoreCase(BotCommand.CREATE_MEMBER_TASK.str());
    }

    public boolean isTaskDeletion() {
        return command.equals(BotCommand.REMOVE_TEAM_TASK.str())
                || command.equalsIgnoreCase(BotCommand.REMOVE_MEMBER_TASK.str());
    }

    public boolean isTaskStatusChanging() {
        return command.equals(BotCommand.UPDATE_TASK_STATUS.str());
    }

    public boolean isTaskViewing(){
        return command.equals(BotCommand.SHOW_TEAM_TASKS.str())
                || command.equalsIgnoreCase(BotCommand.SHOW_MEMBER_TASKS.str());
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
