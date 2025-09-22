package org.linker.plnm.bot.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MessageValidation;
import org.linker.plnm.bot.services.actions.BaseActions;
import org.linker.plnm.bot.services.actions.TeamingActions;
import org.linker.plnm.enums.BotCommand;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service @Slf4j
public class MessageUpdateHandler {

    private final MessageValidation messageValidation;

    private final BaseActions baseActions;

    private final TeamingActions teamingActions;

    public MessageUpdateHandler(
            @Lazy MessageValidation messageValidation,
            BaseActions baseActions,
            TeamingActions teamingActions
    ) {
        this.messageValidation = messageValidation;
        this.baseActions = baseActions;
        this.teamingActions = teamingActions;
    }


    public BotApiMethod<?> handle(@NotNull Message message, String argument, BotCommand command) {
        BotApiMethod<?> response = null;
        long chatId = message.getChatId();
        long  userId = message.getFrom().getId();
        int messageId = message.getMessageId();
        if (messageValidation.illegalCommand(command, chatId, userId, message))
            return null;
        switch (command) {
            case START -> response = baseActions.onBotStart(message.getFrom(), chatId, messageId, messageValidation.isGroup(message));
            case COMMANDS -> response = baseActions.commandsList(chatId);
            case TASKS_MENU -> response = baseActions.tasksMenu(chatId, messageId);
            case CREATE_TEAM -> response = teamingActions.createTeam(chatId, message.getChat().getTitle(), argument);
            case REMOVE_TEAM -> response = teamingActions.removeTeam(chatId, argument);
            case EDIT_TEAM_MENU -> response = teamingActions.editTeam(chatId, argument);
            case SHOW_TEAMS -> response = teamingActions.showTeams(chatId);
            case MY_TEAMS -> response = teamingActions.myTeams(chatId, userId);
        }
        return response;
    }
}
