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
        var teamMenuBtn = KeyboardBuilder.buildButton("❖ Teams Action Menu", BotCommand.TEAMS_MENU.str());
        var markup = KeyboardBuilder.buildVerticalMenu(commandsBtn, teamMenuBtn);
        return MessageBuilder.buildMessage(
                message, BotMessage.START_RESPONSE.format(), MessageParseMode.HTML, markup);
    }

    public static SendMessage botPVStartMenu(Message message) {
        var commandsBtn = KeyboardBuilder.buildButton("❖ Commands", BotCommand.COMMANDS.str());
        var markup = KeyboardBuilder.buildVerticalMenu(commandsBtn);
        return MessageBuilder.buildMessage(
                message, BotMessage.START_RESPONSE.format(), MessageParseMode.HTML, markup);
    }

    public static EditMessageText teamsMenuBackward(Message message) {
        return MessageBuilder.buildEditMessageText(
                message, BotMessage.TEAMS_MENU_HEADER.format(), teamsActionsMarkup());
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
