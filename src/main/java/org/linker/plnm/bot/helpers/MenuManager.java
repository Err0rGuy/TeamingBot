package org.linker.plnm.bot.helpers;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.enums.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MenuManager {

    public static InlineKeyboardMarkup startMenu() {
        var commandsBtn = KeyboardBuilder.buildButton("❖ Commands...", BotCommand.COMMANDS.str());
        var taskMenuBtn = KeyboardBuilder.buildButton("❖ Tasks Action menu", BotCommand.TASKS_MENU_NEW.str());
        return KeyboardBuilder.buildVerticalMenu(commandsBtn, taskMenuBtn);
    }

    @NotNull
    public static InlineKeyboardMarkup taskingActionsMenu() {
        var createTaskMenuBtn = KeyboardBuilder.buildButton("✜ Create Task", BotCommand.CREATE_TASK_MENU.str());
        var removeTaskMenuBtn = KeyboardBuilder.buildButton("✜ Remove Task", BotCommand.REMOVE_TASK_MENU.str());
        var changeTaskStatusMenu = KeyboardBuilder.buildButton("✜ Change Task Status", BotCommand.CH_TASK_STATUS_MENU.str());
        return KeyboardBuilder.buildVerticalMenu(createTaskMenuBtn, removeTaskMenuBtn, changeTaskStatusMenu);
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
}
