package org.linker.plnm.bot.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageValidation;
import org.linker.plnm.bot.services.actions.BaseActions;
import org.linker.plnm.bot.services.actions.TeamingActions;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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


    public BotApiMethod<?> handle(@NotNull Message message, BotCommand command) {
        SendMessage response = null;
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        if (messageValidation.lackOfAccess(command, message.getChatId(), message.getFrom().getId())) {
            response = new SendMessage();
            response.setText(BotMessage.ONLY_ADMIN.format());
            return response;
        }
        switch (command) {
            case START -> response = baseActions.onBotStart(message.getFrom(), chatId, messageId, messageValidation.isGroup(message));
            case COMMANDS -> response = baseActions.commandsList(chatId);
            case TASKS_MENU -> response = MenuManager.tasksMenu(chatId, messageId);
            case TEAMS_MENU -> response = MenuManager.teamsMenu(chatId, messageId);
        }
        return response;
    }
}
