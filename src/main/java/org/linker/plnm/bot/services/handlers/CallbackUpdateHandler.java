package org.linker.plnm.bot.services.handlers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.linker.plnm.bot.helpers.MenuManager;
import org.linker.plnm.bot.helpers.MessageBuilder;
import org.linker.plnm.bot.helpers.MessageValidation;
import org.linker.plnm.bot.services.actions.BaseActions;
import org.linker.plnm.bot.services.actions.TaskingActions;
import org.linker.plnm.bot.services.actions.TeamingActions;
import org.linker.plnm.enums.BotCommand;
import org.linker.plnm.enums.BotMessage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service @Slf4j
public class CallbackUpdateHandler {

    private final MessageValidation messageValidation;

    private final BaseActions baseActions;

    private final TeamingActions teamingActions;

    private final TaskingActions taskingActions;

    public CallbackUpdateHandler(
            @Lazy MessageValidation messageValidation,
            BaseActions baseActions,
            TeamingActions teamingActions,
            TaskingActions taskingActions
    ) {
        this.messageValidation = messageValidation;
        this.baseActions = baseActions;
        this.teamingActions = teamingActions;
        this.taskingActions = taskingActions;
    }

    public BotApiMethod<?> handle(@NotNull Message message, String argument, BotCommand command) {
        BotApiMethod<?> response = null;
        long chatId = message.getChatId();
        long  userId = message.getFrom().getId();
        int messageId = message.getMessageId();
        if (messageValidation.illegalCommand(command, chatId, userId, message))
            return null;
        switch (command) {
            case COMMANDS ->
                    response = baseActions.commandsList(chatId);
            case RENAME_TEAM ->
                    response = teamingActions.validateEditingAction(chatId, userId, argument, command, "new name");
            case REMOVE_MEMBER, ADD_MEMBER ->
                    response = teamingActions.validateEditingAction(chatId, userId, argument, command, "username's");
            case CREATE_TASK_MENU ->
                    response = MessageBuilder.buildEditMessageText(chatId, messageId, BotMessage.TASK_CREATION_MENU_HEADER.format(),
                            MenuManager.taskCreationMenu());
            case REMOVE_TASK_MENU ->
                    response = MessageBuilder.buildEditMessageText(chatId, messageId, BotMessage.TASK_DELETION_MENU_HEADER.format(),
                            MenuManager.taskRemoveMenu());
            case CH_TASK_STATUS_MENU ->
                    response = MessageBuilder.buildEditMessageText(chatId, messageId, BotMessage.TASK_CH_STATUS_MENU_HEADER.format(),
                            MenuManager.taskChangeStatusMenu());
            case TASKS_MENU ->
                    response = MessageBuilder.buildEditMessageText(chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(),
                            MenuManager.taskingActionsMenu());
            case TASKS_MENU_NEW ->
                    response = MessageBuilder.buildMessage(chatId, messageId, BotMessage.TASKS_MENU_HEADER.format(),
                            MenuManager.taskingActionsMenu());
            case CREATE_TEAM_TASK, REMOVE_TEAM_TASK, CH_TEAM_TASK_STATUS ->
                    response = taskingActions.askForArgs(chatId, userId, "team name",command);
            case CREATE_MEMBER_TASK, REMOVE_MEMBER_TASK, CH_MEMBER_TASK_STATUS ->
                    response = taskingActions.askForArgs(chatId, userId, "usernames", command);
        }
        return response;
    }
}
