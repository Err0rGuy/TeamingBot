package org.linker.plnm.bot.helpers;

import org.jetbrains.annotations.NotNull;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MenuManager {


    @NotNull
    public static SendMessage botStartMenu(Long chatId, Integer messageId) {
        var commandsBtn = KeyboardBuilder.buildButton("❖ Commands", BotCommand.HINT.str());
        var taskMenuBtn = KeyboardBuilder.buildButton("❖ Tasks Action menu", BotCommand.TASKS_MENU.str());
        var teamMenuBtn = KeyboardBuilder.buildButton("❖ Teams Action menu", BotCommand.TEAMS_MENU.str());
        var markup = KeyboardBuilder.buildVerticalMenu(commandsBtn, teamMenuBtn, taskMenuBtn);
        return MessageBuilder.buildMessage(
                chatId, messageId, BotMessage.START_RESPONSE.format(), "HTML", markup);
    }

    @NotNull
    public static SendMessage privateChatbotStartMenu(Long chatId, Integer messageId) {
        var commandsBtn = KeyboardBuilder.buildButton("❖ Commands", BotCommand.HINT.str());
        var markup = KeyboardBuilder.buildVerticalMenu(commandsBtn);
        return MessageBuilder.buildMessage(
                chatId, messageId, BotMessage.START_RESPONSE.format(), "HTML", markup);
    }

    /// Tasks action menu
    @NotNull
    public static SendMessage tasksMenu(Long chatId, Integer messageId) {
        return MessageBuilder.buildMessage(
                chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(), taskingActionsMarkup());
    }

    /// Tasks action menu in new message
    @NotNull
    public static EditMessageText tasksMenuBack(Long chatId, Integer messageId) {
        return MessageBuilder.buildEditMessageText(
                chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(), taskingActionsMarkup());
    }

    @NotNull
    public static SendMessage teamsMenu(Long chatId, Integer messageId) {
        return MessageBuilder.buildMessage(
                chatId, messageId, BotMessage.TEAMS_MENU_HEADER.format(), teamingActionsMarkup());
    }

    @NotNull
    public static EditMessageText teamsMenuBack(Long chatId, Integer messageId) {
        return MessageBuilder.buildEditMessageText(
                chatId, messageId, BotMessage.TEAMS_MENU_HEADER.format(), teamingActionsMarkup());
    }

    @NotNull
    public static SendMessage editTeamMenu(Long chatId, Integer messageId, String teamName) {
        var renameBtn = KeyboardBuilder.buildButton("➾ Rename Team", BotCommand.RENAME_TEAM.str() + " " + teamName);
        var addMemberBtn = KeyboardBuilder.buildButton("➾ Add Member", BotCommand.ADD_MEMBER.str() + " " + teamName);
        var removeMemberBtn = KeyboardBuilder.buildButton("➾ Remove Member", BotCommand.REMOVE_MEMBER.str() + " " + teamName);
        var markup = KeyboardBuilder.buildVerticalMenu(renameBtn, addMemberBtn, removeMemberBtn);
        return MessageBuilder.buildMessage(
                chatId, messageId, BotMessage.ASK_FOR_EDIT_OPTIONS.format(teamName), markup);
    }

    @NotNull
    public static EditMessageText createTaskMenu(Long chatId, Integer messageId) {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.CREATE_TEAM_TASK.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.CREATE_MEMBER_TASK.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU_BACK.str());
        var markup = KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
        return MessageBuilder.buildEditMessageText(chatId, messageId,
                BotMessage.TASK_CREATION_MENU_HEADER.format(), markup);
    }

    @NotNull
    public static EditMessageText removeTaskMenu(Long chatId, Integer messageId) {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.REMOVE_TEAM_TASK.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.REMOVE_MEMBER_TASK.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU_BACK.str());
        var markup = KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
        return MessageBuilder.buildEditMessageText(chatId, messageId,
                BotMessage.TASK_DELETION_MENU_HEADER.format(), markup);
    }

    @NotNull
    public static EditMessageText showTasksMenu(Long chatId, Integer messageId) {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.SHOW_TEAM_TASKS.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.SHOW_MEMBER_TASKS.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU_BACK.str());
        var markup = KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
        return MessageBuilder.buildEditMessageText(chatId, messageId,
                BotMessage.TASK_SHOWING_MENU_HEADER.format(), markup);
    }

    @NotNull
    private static InlineKeyboardMarkup taskingActionsMarkup() {
        var createTaskMenuBtn = KeyboardBuilder.buildButton("✜ Create Task", BotCommand.CREATE_TASK_MENU.str());
        var removeTaskMenuBtn = KeyboardBuilder.buildButton("✜ Remove Task", BotCommand.REMOVE_TASK_MENU.str());
        var changeTaskStatusMenuBtn = KeyboardBuilder.buildButton("✜ Change Task Status", BotCommand.UPDATE_TASK_STATUS.str());
        var seeTasksMenuBtn = KeyboardBuilder.buildButton("✜ See Tasks", BotCommand.SHOW_TASKS_MENU.str());
        return KeyboardBuilder.buildVerticalMenu(createTaskMenuBtn, removeTaskMenuBtn, changeTaskStatusMenuBtn, seeTasksMenuBtn);
    }

    @NotNull
    private static InlineKeyboardMarkup teamingActionsMarkup() {
        var createTeamBtn = KeyboardBuilder.buildButton("✜ Create Team", BotCommand.CREATE_TEAM.str());
        var removeTeamBtn = KeyboardBuilder.buildButton("✜ Remove Team", BotCommand.REMOVE_TEAM.str());
        var editTeamBtn = KeyboardBuilder.buildButton("✜ Edit Team", BotCommand.EDIT_TEAM_MENU.str());
        var showTeamsBtn = KeyboardBuilder.buildButton("✜ Show Teams", BotCommand.SHOW_TEAMS.str());
        var showMyTeamsBtn = KeyboardBuilder.buildButton("✜ My Teams", BotCommand.MY_TEAMS.str());
        return KeyboardBuilder.buildVerticalMenu(createTeamBtn, removeTeamBtn, editTeamBtn, showTeamsBtn, showMyTeamsBtn);
    }
}
