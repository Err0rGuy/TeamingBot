package org.linker.plnm.bot.helpers.builders;

import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.linker.plnm.enums.MessageParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public class MenuBuilder {


    public static SendMessage startMenu(Message message) {
        var commandsBtn = KeyboardBuilder.buildButton("❖ Commands", BotCommand.COMMANDS.str());
        var taskMenuBtn = KeyboardBuilder.buildButton("❖ Tasks Action Menu", BotCommand.TASKS_MENU.str());
        var teamMenuBtn = KeyboardBuilder.buildButton("❖ Teams Action Menu", BotCommand.TEAMS_MENU.str());
        var markup = KeyboardBuilder.buildVerticalMenu(commandsBtn, teamMenuBtn, taskMenuBtn);
        return MessageBuilder.buildMessage(
                message, BotMessage.START_RESPONSE.format(), MessageParseMode.HTML, markup);
    }

    public static SendMessage botPVStartMenu(Message message) {
        var commandsBtn = KeyboardBuilder.buildButton("❖ Commands", BotCommand.COMMANDS.str());
        var markup = KeyboardBuilder.buildVerticalMenu(commandsBtn);
        return MessageBuilder.buildMessage(
                message, BotMessage.START_RESPONSE.format(), MessageParseMode.HTML, markup);
    }

    public static SendMessage tasksMenu(Message message) {
        return MessageBuilder.buildMessage(
                message, BotMessage.TASKS_MENU_HEADER.format(), tasksActionsMarkup());
    }

    public static EditMessageText tasksMenuBackward(Message message) {
        return MessageBuilder.buildEditMessageText(
                message, BotMessage.TASKS_MENU_HEADER.format(), tasksActionsMarkup());
    }

    public static EditMessageText teamsMenuBackward(Message message) {
        return MessageBuilder.buildEditMessageText(
                message, BotMessage.TASKS_MENU_HEADER.format(), teamsActionsMarkup());
    }

    public static SendMessage teamsMenu(Message message) {
        return MessageBuilder.buildMessage(
                message, BotMessage.TEAMS_MENU_HEADER.format(), teamsActionsMarkup());
    }

    public static SendMessage editTeamMenu(Message message, String teamName) {
        var renameBtn = KeyboardBuilder.buildButton("➾ Rename Team", BotCommand.RENAME_TEAM.str() + " " + teamName);
        var addMemberBtn = KeyboardBuilder.buildButton("➾ Add Member", BotCommand.ADD_MEMBER.str() + " " + teamName);
        var removeMemberBtn = KeyboardBuilder.buildButton("➾ Remove Member", BotCommand.REMOVE_MEMBER.str() + " " + teamName);
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TEAMS_MENU.str() + " " + BotCommand.BACKWARD.str());
        var markup = KeyboardBuilder.buildVerticalMenu(renameBtn, addMemberBtn, removeMemberBtn, backBtn);
        return MessageBuilder.buildMessage(
                message, BotMessage.ASK_FOR_EDIT_OPTIONS.format(teamName), markup);
    }

    public static EditMessageText taskCreationMenu(Message message) {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.CREATE_TEAM_TASK.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.CREATE_MEMBER_TASK.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU.str() + " " + BotCommand.BACKWARD.str());
        var markup = KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
        return MessageBuilder.buildEditMessageText(
                message, BotMessage.TASK_CREATION_MENU_HEADER.format(), markup);
    }

    public static EditMessageText taskDeletionMenu(Message message) {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.REMOVE_TEAM_TASK.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.REMOVE_MEMBER_TASK.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU.str() + " " + BotCommand.BACKWARD.str());
        var markup = KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
        return MessageBuilder.buildEditMessageText(
                message, BotMessage.TASK_DELETION_MENU_HEADER.format(), markup);
    }

    public static EditMessageText showTasksMenu(Message message) {
        var createTeamTaskBtn = KeyboardBuilder.buildButton("⬧ Team Task", BotCommand.SHOW_TEAM_TASKS.str());
        var createMemberTaskBtn = KeyboardBuilder.buildButton("⬧ Member Task", BotCommand.SHOW_MEMBER_TASKS.str());
        var backBtn = KeyboardBuilder.buildButton("⟵ Back", BotCommand.TASKS_MENU_BACKWARD.str());
        var markup = KeyboardBuilder.buildVerticalMenu(createTeamTaskBtn, createMemberTaskBtn, backBtn);
        return MessageBuilder.buildEditMessageText(
                message, BotMessage.TASK_SHOWING_MENU_HEADER.format(), markup);
    }

    private static InlineKeyboardMarkup tasksActionsMarkup() {
        var createTaskMenuBtn = KeyboardBuilder.buildButton("✜ Create Task", BotCommand.CREATE_TASK_MENU.str());
        var removeTaskMenuBtn = KeyboardBuilder.buildButton("✜ Remove Task", BotCommand.REMOVE_TASK_MENU.str());
        var changeTaskStatusMenuBtn = KeyboardBuilder.buildButton("✜ Change Task Status", BotCommand.UPDATE_TASK_STATUS.str());
        var seeTasksMenuBtn = KeyboardBuilder.buildButton("✜ See Tasks", BotCommand.SHOW_TASKS_MENU.str());
        return KeyboardBuilder.buildVerticalMenu(
                createTaskMenuBtn, removeTaskMenuBtn, changeTaskStatusMenuBtn, seeTasksMenuBtn);
    }

    private static InlineKeyboardMarkup teamsActionsMarkup() {
        var createTeamBtn = KeyboardBuilder.buildButton("✜ Create Team", BotCommand.CREATE_TEAM.str());
        var removeTeamBtn = KeyboardBuilder.buildButton("✜ Remove Team", BotCommand.REMOVE_TEAM.str());
        var editTeamBtn = KeyboardBuilder.buildButton("✜ Edit Team", BotCommand.EDIT_TEAM_MENU.str());
        var showTeamsBtn = KeyboardBuilder.buildButton("✜ Show Teams", BotCommand.SHOW_TEAMS.str());
        var showMyTeamsBtn = KeyboardBuilder.buildButton("✜ My Teams", BotCommand.MY_TEAMS.str());
        return KeyboardBuilder.buildVerticalMenu(
                createTeamBtn, removeTeamBtn, editTeamBtn, showTeamsBtn, showMyTeamsBtn);
    }
}
