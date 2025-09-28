package org.linker.plnm.enums;

import java.util.Arrays;
import java.util.List;

public enum BotCommand {
    START("/start", List.of(CommandType.UNPRIVILEGED, CommandType.PV_ALLOWED)),
    COMMANDS("/commands", List.of(CommandType.UNPRIVILEGED, CommandType.PV_ALLOWED)),
    CREATE_TEAM("/create_team", List.of(CommandType.PRIVILEGED)),
    REMOVE_TEAM("/remove_team", List.of(CommandType.PRIVILEGED)),
    RENAME_TEAM("/rename_team", List.of(CommandType.PRIVILEGED)),
    ADD_MEMBER("/add_member", List.of(CommandType.PRIVILEGED)),
    REMOVE_MEMBER("/remove_member", List.of(CommandType.PRIVILEGED)),
    SHOW_TEAMS("/show_teams", List.of(CommandType.UNPRIVILEGED)),
    MY_TEAMS("/my_teams", List.of(CommandType.UNPRIVILEGED)),
    TASKS_MENU("/tasks_menu", List.of(CommandType.UNPRIVILEGED)),
    SHOW_TASKS_MENU("menu:seeTask", List.of(CommandType.PRIVILEGED)),
    SHOW_MEMBER_TASKS("/see_member_tasks", List.of(CommandType.PRIVILEGED)),
    TASKS_MENU_BACKWARD("/tasks_menu_backward", List.of(CommandType.UNPRIVILEGED)),
    EDIT_TEAM_MENU("/edit_team", List.of(CommandType.PRIVILEGED)),
    TEAMS_MENU("/teams_menu", List.of(CommandType.UNPRIVILEGED)),
    TEAMS_MENU_BACKWARD("/teams_menu_backward", List.of(CommandType.UNPRIVILEGED)),
    CREATE_TASK_MENU("menu:createTask", List.of(CommandType.PRIVILEGED)),
    REMOVE_TASK_MENU("menu:removeTask", List.of(CommandType.PRIVILEGED)),
    CREATE_MEMBER_TASK("/create_member_task", List.of(CommandType.PRIVILEGED)),
    REMOVE_MEMBER_TASK("/remove_member_task", List.of(CommandType.PRIVILEGED)),
    CREATE_TEAM_TASK("/create_team_task", List.of(CommandType.PRIVILEGED)),
    REMOVE_TEAM_TASK("/remove_team_task", List.of(CommandType.PRIVILEGED)),
    UPDATE_TASK_STATUS("/update_task_status", List.of(CommandType.PRIVILEGED)),
    SHOW_TEAM_TASKS("/see_team_tasks", List.of(CommandType.PRIVILEGED));

    public enum CommandType {
        PRIVILEGED,
        UNPRIVILEGED,
        PV_ALLOWED
    }

    private final String command;

    private final List<CommandType> types;

    BotCommand(String command, List<CommandType> types){
        this.command = command;
        this.types = types;
    }

    public String str() {
        return command;
    }

    public boolean isPrivileged() {
        return types.contains(CommandType.PRIVILEGED);
    }

    public boolean isOfType(CommandType type){
        return types.contains(type);
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

    public static BotCommand getCommand(String text) {
        return Arrays.stream(values())
                .filter(c -> text.equals(c.str())).findFirst().orElse(null);
    }
}
