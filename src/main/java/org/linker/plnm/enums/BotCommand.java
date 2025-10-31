package org.linker.plnm.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    EDIT_TEAM_MENU("/edit_team", List.of(CommandType.PRIVILEGED)),
    TEAMS_MENU("/teams_menu", List.of(CommandType.UNPRIVILEGED)),
    TEAMS_MENU_BACKWARD("/teams_menu_backward", List.of(CommandType.UNPRIVILEGED)),
    BACKWARD("/backward", List.of(CommandType.PRIVILEGED));

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

    public boolean isPvAllowed() {
        return types.contains(CommandType.PV_ALLOWED);
    }

    public static BotCommand getCommand(String text) {
        return Arrays.stream(values())
                .filter(c -> Objects.equals(text, c.str()))
                .findFirst()
                .orElse(null);
    }
}
