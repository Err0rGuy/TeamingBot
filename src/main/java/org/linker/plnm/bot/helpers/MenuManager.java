package org.linker.plnm.bot.helpers;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.enums.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MenuManager {

    @NotNull
    public static InlineKeyboardMarkup startMenu() {
        var commandsBtn = KeyboardBuilder.buildButton("❖ Commands...", BotCommand.COMMANDS.str());
        var taskMenuBtn = KeyboardBuilder.buildButton("❖ Tasks Action menu", BotCommand.TASKS_MENU_NEW.str());
        return KeyboardBuilder.buildVerticalMenu(commandsBtn, taskMenuBtn);
    }

    @NotNull
    public static InlineKeyboardMarkup startMenuInPrivateChat() {
        var commandsBtn = KeyboardBuilder.buildButton("❖ Commands...", BotCommand.COMMANDS.str());
        return KeyboardBuilder.buildVerticalMenu(commandsBtn);
    }

    @NotNull
    public static InlineKeyboardMarkup teamingActionsMenu() {
        var createTeamBtn = KeyboardBuilder.buildButton("✜ Create Team", BotCommand.CREATE_TEAM.str());
        var removeTeamBtn = KeyboardBuilder.buildButton("✜ Remove Team", BotCommand.REMOVE_TEAM.str());
        var editTeamBtn = KeyboardBuilder.buildButton("✜ Edit Team", BotCommand.EDIT_TEAM_MENU.str());
        var showTeamsBtn = KeyboardBuilder.buildButton("✜ Show Teams", BotCommand.SHOW_TEAMS.str());
        var showMyTeamsBtn = KeyboardBuilder.buildButton("✜ My Teams", BotCommand.MY_TEAMS.str());
        return KeyboardBuilder.buildVerticalMenu(createTeamBtn, removeTeamBtn, editTeamBtn, showTeamsBtn, showMyTeamsBtn);
    }


    @NotNull
    public static InlineKeyboardMarkup editTeamMenu(String teamName) {
        var renameBtn = KeyboardBuilder.buildButton("➾ Rename Team", BotCommand.RENAME_TEAM.str() + " " + teamName);
        var addMemberBtn = KeyboardBuilder.buildButton("➾ Add Member", BotCommand.ADD_MEMBER.str() + " " + teamName);
        var removeMemberBtn = KeyboardBuilder.buildButton("➾ Remove Member", BotCommand.REMOVE_MEMBER.str() + " " + teamName);
        return KeyboardBuilder.buildVerticalMenu(renameBtn, addMemberBtn, removeMemberBtn);
    }

    @NotNull
    public static InlineKeyboardMarkup taskingActionsMenu() {
        var createTaskMenuBtn = KeyboardBuilder.buildButton("✜ Create Task", BotCommand.CREATE_TASK_MENU.str());
        var removeTaskMenuBtn = KeyboardBuilder.buildButton("✜ Remove Task", BotCommand.REMOVE_TASK_MENU.str());
        var changeTaskStatusMenuBtn = KeyboardBuilder.buildButton("✜ Change Task Status", BotCommand.CH_TASK_STATUS_MENU.str());
        var seeTasksMenuBtn = KeyboardBuilder.buildButton("✜ See Tasks", BotCommand.SEE_TASKS_MENU.str());
        return KeyboardBuilder.buildVerticalMenu(createTaskMenuBtn, removeTaskMenuBtn, changeTaskStatusMenuBtn, seeTasksMenuBtn);
    }

    @NotNull
    public static InlineKeyboardMarkup taskCreationMenu() {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.CREATE_TEAM_TASK.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.CREATE_MEMBER_TASK.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU.str());
        return KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
    }

    @NotNull
    public static InlineKeyboardMarkup taskRemoveMenu() {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.REMOVE_TEAM_TASK.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.REMOVE_MEMBER_TASK.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU.str());
        return KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
    }

    @NotNull
    public static InlineKeyboardMarkup taskChangeStatusMenu() {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.CH_TEAM_TASK_STATUS.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.CH_MEMBER_TASK_STATUS.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU.str());
        return KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
    }

    @NotNull
    public static InlineKeyboardMarkup seeTasksMenu() {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.SEE_TEAM_TASKS.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.SEE_MEMBER_TASKS.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU.str());
        return KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
    }
}
